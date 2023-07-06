public class Test {
    public int uu = 1;

    public static void main(String[] args) {
        int a = 1;
        float b = (float) (a / 0.2f);
        System.out.println(b);

    }

    public boolean some(int x) {
        int y = x - this.uu;
        float u = 0.0f;
        for (int i = 0; i < y; i++) {
            u += i / 0.7f;
        }
        if (u > 100) {
            u = u + u;
        } else {
            u = 1 / u;
        }
        u = fff("http:" + u + "/" + this);
        // System.out.println(u);
        return u < 0.9;
    }

    public float fff(String x) {
        return 0.5;
    }

}
