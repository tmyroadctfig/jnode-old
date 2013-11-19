package org.jnode.fs.hfsplus.tree;

import java.util.LinkedList;
import org.apache.log4j.Logger;

public abstract class AbstractIndexNode<K extends Key> extends AbstractNode<IndexRecord> {

    private static final Logger log = Logger.getLogger(AbstractIndexNode.class);

    /**
     * Create a new node.
     *
     * @param descriptor
     * @param nodeSize
     */
    public AbstractIndexNode(NodeDescriptor descriptor, final int nodeSize) {
        super(descriptor, nodeSize);
    }

    /**
     * Create node from existing data.
     *
     * @param nodeData
     * @param nodeSize
     */
    public AbstractIndexNode(final byte[] nodeData, final int nodeSize) {
        super(nodeData, nodeSize);

    }

    /**
     * Creates a key for the node.
     *
     * @param nodeData the node data.
     * @param offset the offset the key is at.
     * @return the key.
     */
    protected abstract K createKey(byte[] nodeData, int offset);

    @Override
    protected void loadRecords(final byte[] nodeData) {
        int offset;
        for (int i = 0; i < this.descriptor.getNumRecords(); i++) {
            offset = offsets.get(i);
            Key key = createKey(nodeData, offset);
            records.add(new IndexRecord(key, nodeData, offset));

            if (log.isDebugEnabled()) {
                log.debug("Loading index record: " + key);
            }
        }
    }

    /**
     * Find node record based on it's key.
     *
     * @param key The key to search.
     * @return a NodeRecord or {@code null}
     */
    public IndexRecord find(final K key) {
        for (int index = 0; index < this.getNodeDescriptor().getNumRecords(); index++) {
            IndexRecord record = this.getNodeRecord(index);
            if ((record.getKey().equals(key))) {
                return record;
            }
        }

        return null;
    }

    /**
     * Finds all records with the given parent key.
     *
     * @param key the parent key.
     * @return an array of NodeRecords
     */
    public final IndexRecord[] findAll(final K key) {
        LinkedList<IndexRecord> result = new LinkedList<IndexRecord>();
        IndexRecord largestMatchingRecord = null;
        K largestMatchingKey = null;

        for (IndexRecord record : records) {
            K recordKey = (K) record.getKey();

            if (recordKey.compareTo(key) < 0) {
                // The keys/records should be sorted in this index record so take the highest key less than the parent
                largestMatchingKey = recordKey;
                largestMatchingRecord = record;
            } else if (recordKey.equals(key)) {
                result.addLast(record);
            }
        }

        if (largestMatchingKey != null) {
            result.addFirst(largestMatchingRecord);
        }

        return result.toArray(new IndexRecord[result.size()]);
    }
}

