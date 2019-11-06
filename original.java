public class Helloworld{
 
    public static void main(String args[]){
        int a = 100;
        int b = 10;
        int c = 0;
        System.out.println(abc(a,b,c));
        //correct result 2100
        
    }
    
    public static int abc(int a,int b, int c){
        c=a+b;
        c+=a;
        c*=b;
        a=c/b;
        //comments are ignored
        return  (c-b);
    }
}
