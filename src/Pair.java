public class Pair {
    String key;
    String val;
    public Pair(){

    }
    public Pair(String key, String val){
        this.key = key;
        this.val = val;
    }
    public String toString(){
        return key +" -> "+val;
    }

}
