public class Main {
    
    public static void main(String[] args){
        LinklessTree<Integer> tree = new LinklessTree<>(17);
        for (int i = 100; i > 0; i--) {
            tree.insert(i);
        }
        System.out.println(tree.contains(50));
        System.out.println(tree.findIndex(42));
        System.out.println(tree.delete(21));
    }
}
