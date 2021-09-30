import java.util.Arrays;

/**
 * Trees without explicit links. TODO finish this
 * 
 * @author Stefan Kahrs, Dylan Donovan
 * @version 1
 */
@SuppressWarnings("unchecked")
public class LinklessTree<T extends Comparable<? super T>> {
    // sizes of subtrees at that node index
    private int[] sizes;
    // for annoying technical reason this has to be an array of objects
    private Object[] elems;
    private int weightFactor;

    public LinklessTree() {
        this(16, 3);
    }

    public LinklessTree(int startSize) {
        this(startSize, 3);
    }

    /**
     * Constructs a new empty tree using the specified parameters.
     * 
     * startSize is the intial size of the array backing the tree. If startSize is
     * not a square of 2, it is rounded up to the nearest square of 2.
     * 
     * weightFactor is used when deciding if a tree is unbalanced, using
     * {@code x > (y * weightFactor)} where x and y are the sizes of the subnodes of
     * any given node.
     * 
     * @param startSize    the intial size of the tree
     * @param weightFactor factor used to decide when a tree is unbalanced
     */
    public LinklessTree(int startSize, int weightFactor) {
        if (startSize < 1) {
            throw new IllegalArgumentException("invalid startSize:" + startSize);
        } else if ((startSize & (startSize - 1)) == 0) {
            elems = freshElemArray(startSize);
            sizes = new int[startSize];
            this.weightFactor = weightFactor;
        } else {
            startSize = Integer.highestOneBit(startSize) * 2;
            elems = freshElemArray(startSize);
            sizes = new int[startSize];
            this.weightFactor = weightFactor;
        }
    }

    // size of whole tree is the size of the subtree rooted at 0
    public int size() {
        return getSize(0);
    }

    public T getValue(int index) {
        return (T) elems[index];
    }

    // auxiliary methods to index the arrays out of bounds too
    // they may help to reduce case distinctions
    private T getKey(int subtree) {
        if (subtree >= elems.length)
            return null; // out of bounds
        return getValue(subtree);
    }

    private int getSize(int subtree) {
        if (subtree >= elems.length)
            return 0; // out of bounds
        return sizes[subtree];
    }

    // encapsulates the cast on the allocation
    private Object[] freshElemArray(int capacity) {
        return new Object[capacity];
    }

    // remainder needs to be modified

    // find index position of val in tree, if there, or where it goes, if not there
    public int findIndex(T val) {
        int i = 0;
        for (;;) {
            if (i >= elems.length || elems[i] == null)
                return i; // Where val would go
            else if (val.compareTo((T) elems[i]) == 0)
                return i; // Found val
            else if (val.compareTo((T) elems[i]) < 0)
                i = (2 * i) + 1; // Go left
            else if (val.compareTo((T) elems[i]) > 0)
                i = (2 * i) + 2; // Go right
        }
    }

    // is value in tree
    public boolean contains(T val) {

        for (int i = 0;;) {
            if (i >= elems.length || elems[i] == null)
                return false; // Where val would go
            else if (val.compareTo((T) elems[i]) == 0)
                return true; // Found val
            else if (val.compareTo((T) elems[i]) < 0)
                i = (2 * i) + 1; // Go left
            else if (val.compareTo((T) elems[i]) > 0)
                i = (2 * i) + 2; // Go right
        }
    }

    // grow the space in which we can place the tree, so that at least one insertion
    // will succeed
    private void grow() {
        elems = Arrays.copyOf(elems, elems.length << 1);
        sizes = Arrays.copyOf(sizes, sizes.length << 1);
    }

    // fetch the i-th element, in comparsion order
    public T get(int i) {
        if (i >= elems.length || i < 0)
            return null; // out of bounds

        int target = i;

        for (int j = 0;;) {
            if ((target - sizes[2 * j + 1]) == 0) {
                return (T) elems[j];
            } else if ((target - sizes[2 * j + 1]) < 0) {
                j = (2 * j) + 1;
            } else {
                target = target - (sizes[2 * j + 1] + 1);
                j = (2 * j) + 2;
            }

        }
    }

    /*
     * !!CONTEXT
     * 
     * The rotations in a linkless tree are a more complex operation to preform,
     * even though the primary operation is the same as any other BST.
     * 
     * When a BST with links is rotated, the nodes that move maintain their links to
     * their subnodes. In this BST the "link" to subnodes is the ordering of the
     * array. Specifically for a node at index i, its left subnode has an index of 2
     * * i + 1, and the right subnode has index 2 * i + 2.
     * 
     * This means of subnodes of the node that is moved "up", one of them has to be
     * moved "up"(Group 3) and the other has to be moved "across"(Group 2). Then all
     * of the subnodes of the node that is moved "down", must also be moved
     * "down"(Group 1).
     * 
     * !!ORDER
     * 
     * Of these three groups, only group 1 is guaranteed to be moving into empty
     * space so we must start there. The space created by group 1 moving is filled
     * by group 2 and the space created by group 2 moving is filled by group 3.
     * Importantly group 1 must start moving from the bottom-most nodes, while group
     * 3 must start from the top-most nodes.
     */

    /**
     * Rebalances the tree from the given index {@code i}, to its leaf nodes.
     * 
     * @param i     the index of the tree to be rebalanced
     * @param level the level of the tree to be rebalanced, starting with 0 at root
     *              level.
     */
    private void fixTree(int i, int level) {
        if (elems[i] == null) // End of tree
            return;

        int left = 2 * i + 1;
        int right = 2 * i + 2;
        if ((2 * right + 2) < elems.length) { // Nodes at the bottom 2 levels can't be unbalanced
            if ((sizes[left] < 2) && (sizes[right] < 2)) {
                // Tree with subnode sizes == 1 or 0, don't need balancing
            } else if (sizes[right] > (sizes[left] * weightFactor)) {
                if (sizes[(2 * right + 1)] < sizes[(2 * right + 2)]) {
                    rotateLeft(i);
                } else {
                    rotateRight(right);
                    rotateLeft(i);
                }
            } else if (sizes[left] > (sizes[right] * weightFactor)) {
                if (sizes[(2 * left + 1)] > sizes[(2 * left + 2)]) {
                    rotateRight(i);
                } else {
                    rotateLeft(left);
                    rotateRight(i);
                }
            }

            fixTree(right, level + 1);
            fixTree(left, level + 1);
        }

    }

    /**
     * Moves the tree with root {@code i} horizontally in either direction.
     * 
     * @param i           root of the tree to be moved.
     * @param isRightMove should be true if the tree is moving right, false
     *                    otherwise.
     * @param relLevel    current level of the tree to be moved, not the entire
     *                    tree, starting at 1.
     */
    private void moveAcross(int i, boolean isRightMove, int relLevel) {
        if (i >= elems.length)
            return; // index out of bounds
        // no check for null nodes as we want to overwrite the nodes we are moving to
        // anyway.
        int direction = isRightMove ? 1 : -1;
        elems[i + direction * relLevel] = elems[i];
        sizes[i + direction * relLevel] = sizes[i];
        moveAcross(2 * i + 1, isRightMove, relLevel + 1);
        moveAcross(2 * i + 2, isRightMove, relLevel + 1);
    }

    /**
     * Moves the tree with root {@code i} down and right.
     * 
     * @param i     index of the tree to be moved
     * @param level the level of {@code i} in the tree being moved down. <b>Should
     *              be 0 for the first call</b>
     */
    private void moveDownRight(int i, int level) {
        // limit is how many nodes to the left of i we want to move which increases by
        // 2^level
        int limit = 0B1 << level;
        if (2 * i + 1 >= elems.length)
            grow(); // check the biggest index for this level to move into

        // This null check is a compromise. An alternative would be to call this
        // function on every node to be moved with an "offset" parameter from the
        // rightmost node being moved (the opposite for moveDownLeft). Then each function call would return if its index
        // was null.
        boolean found = false;
        for (int j = i - limit; j < i; j++) {
            if (elems[j] != null) {
                found = true;
                break;
            }
        }
        if (!found) {
            return;
        }

        moveDownRight(2 * i + 2, level + 1);
        for (int j = i - limit, offset = 0; j < i; j++, offset++) {
            elems[2 * j + (2 + offset)] = elems[j];
            sizes[2 * j + (2 + offset)] = sizes[j];
        }
    }

    /**
     * Moves the tree with root {@code i} down and left.
     * 
     * @param i     index of the tree to be moved
     * @param level the level of {@code i} in the tree being moved down. <b>Should
     *              be 0 for the first call</b>
     */
    private void moveDownLeft(int i, int level) {
        int limit = 0B1 << level;
        if (2 * i + 1 >= elems.length)
            grow(); // check the biggest index for this level to move into

        boolean found = false;
        for (int j = i; j < (i + limit); j++) {
            if (elems[j] != null) {
                found = true;
                break;
            }
        }

        if (!found) {
            return;
        }

        moveDownLeft(2 * i + 1, level + 1);
        for (int j = i, offset = 0; j < (i + limit); j++, offset++) {
            elems[2 * j + (1 - offset)] = elems[j];
            sizes[2 * j + (1 - offset)] = sizes[j];
        }
    }

    /**
     * Moves the tree with root {@code i} up in a right rotate.
     * 
     * Since each level of a BST has half the nodes of the level below it, when
     * moving a subtree up it overwrites the subtree to the right of the root.
     * 
     * @param i             index of the node to be moved.
     * @param countFromLeft the distance of the current node from the leftmost node
     *                      at the current level.
     */
    private void moveUpRight(int i, int countFromLeft) {
        if (i >= elems.length)
            return; // index out of bounds
        // no check for null nodes as we want to overwrite the nodes we are moving to
        // anyway.
        elems[(i + (countFromLeft - 1)) / 2] = elems[i];
        elems[i] = null;
        sizes[(i + (countFromLeft - 1)) / 2] = sizes[i];
        sizes[i] = 0;
        moveUpRight(2 * i + 1, countFromLeft);
        moveUpRight(2 * i + 2, countFromLeft + 1);
    }

    /**
     * Moves the tree with root {@code i} up in a left rotate.
     * 
     * Since each level of a BST has half the nodes of the level below it, when
     * moving a subtree up it overwrites the subtree to the left of the root.
     * 
     * @param i              index of the node to be moved.
     * @param countFromRight the distance of the current node from the rightmost
     *                       node at the current level.
     */
    private void moveUpLeft(int i, int countFromRight) {
        if (i >= elems.length)
            return; // index out of bounds
        // no check for null nodes as we want to overwrite the nodes we are moving to
        // anyway.
        elems[(i - (countFromRight + 1)) / 2] = elems[i];
        elems[i] = null;
        sizes[(i - (countFromRight + 1)) / 2] = sizes[i];
        sizes[i] = 0;
        moveUpLeft(2 * i + 1, countFromRight + 1);
        moveUpLeft(2 * i + 2, countFromRight);
    }

    /**
     * Preforms a right rotate from index {@code i}.
     * 
     * @param i     index of the node to be rotated.
     * @param level the level of {@code i} in the tree represented by
     *              {@link #elems}, starting at 0.
     */
    private void rotateRight(int i) {
        // start by moving the group 1 nodes down
        moveDownRight(2 * i + 2, 0);

        // then start the rotate by moving the top node to it's right subnode
        elems[2 * i + 2] = elems[i];

        // next move the group 2 nodes right
        moveAcross(4 * i + 4, true, 1);

        // then finish the rotate by moving the left subnode up
        elems[i] = elems[2 * i + 1];

        // then move group 3 up
        moveUpRight(4 * i + 3, 0);

        // finish by finding the new sizes of the rotated nodes
        sizes[2 * i + 1] = sizes[4 * i + 3] + sizes[4 * i + 4] + 1;
        sizes[2 * i + 2] = sizes[4 * i + 5] + sizes[4 * i + 6] + 1;
        sizes[i] = sizes[2 * i + 1] + sizes[2 * i + 2] + 1;
    }

    private void rotateLeft(int i) {
        // start by moving the group 1 nodes down
        moveDownLeft(2 * i + 1, 0);

        // then start the rotate by moving the top node to it's left subnode
        elems[2 * i + 1] = elems[i];

        // next move the group 2 nodes left
        moveAcross(4 * i + 5, false, 1);

        // then finish the rotate by moving the right subnode up
        elems[i] = elems[2 * i + 2];

        // then move group 3 up
        moveUpLeft(4 * i + 6, 0);

        // finish by finding the new sizes of the rotated nodes
        sizes[2 * i + 1] = sizes[4 * i + 3] + sizes[4 * i + 4] + 1;
        sizes[2 * i + 2] = sizes[4 * i + 5] + sizes[4 * i + 6] + 1;
        sizes[i] = sizes[2 * i + 1] + sizes[2 * i + 2] + 1;
    }

    /**
     * Add {@code x} to tree, return true if tree was modified. Duplicates are not
     * allowed in the tree (determined by the compareTo() implemented by {@code T}).
     * 
     * @param value value to be added to the tree.
     * @return true if tree was modified, false otherwise.
     */
    public boolean insert(T value) {
        int index = findIndex(value);
        if (index >= elems.length)
            grow(); // TODO if grow() is needed we must be adding a new value so we can skip the
                    // checks below for speeeeeed

        if (elems[index] == null) {
            elems[index] = value;

            while (index >= 0) {
                sizes[index]++;
                index = (index % 2) == 0 ? (index - 2) / 2 : (index - 1) / 2;
            }
            fixTree(0, 0);
        } else if (value.compareTo((T) elems[index]) == 0) {
            return false;
        } else {
            if (sizes[2 * index + 1] > sizes[2 * index + 2]) { // TODO: is this reachable?
                moveDownRight(index, (int) Math.sqrt(Integer.highestOneBit(index)));
            } else {
                moveDownLeft(index, (int) Math.sqrt(Integer.highestOneBit(index)));
            }
            elems[index] = value;
        }
        weightCheck();
        return true;
    }

    private void weightCheck() {
        for (int i = 0; 2 * i + 2 < elems.length; i++) {
            if (sizes[i] == 0) {
                // lk
            } else if (sizes[i] != sizes[2 * i + 1] + sizes[2 * i + 2] + 1 || sizes[i] == 0)
                System.out.println("i");
        }
    }

    // remove x from tree, return true if tree was modified
    public boolean delete(T x) {
        int i = findIndex(x);
        if (i >= elems.length || elems[i] == null || x.compareTo((T) elems[i]) != 0)
            return false;

        while (sizes[i] != 0) {
            if (sizes[i] == 1) {
                elems[i] = null;
                sizes[i] = 0;
            } else if (sizes[2 * i + 1] > sizes[2 * i + 2]) {
                elems[i] = elems[2 * i + 1];
                elems[2 * i + 1] = null;
                i = (2 * i) + 1;
            } else {
                elems[i] = elems[2 * i + 2];
                elems[2 * i + 2] = null;
                i = (2 * i) + 2;
            }
        }

        while (i >= 0) {
            sizes[i] = sizes[2 * i + 1] + sizes[2 * i + 2] + 1;
            i = (i % 2) == 0 ? (i - 2) / 2 : (i - 1) / 2;
        }
        return true;
    }
}