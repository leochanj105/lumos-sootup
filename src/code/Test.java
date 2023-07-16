public class Test {

    class T {
        public Boolean body;

        public T(boolean b) {

            this.body = b;
            // this.body
        }

        public Object getBody() {
            // r0 = this <r0, this>
            // r1 = r0.body <r1, r0.body>
            // return r1
            return this.body;
        }
    }

    // class Status {
    // String status = "CANCEL";

    // public String getStatus() {
    // return status;
    // }
    // }

    class Order {
        String status;

        public Order() {
            this.status = "CANCEL";
        }

        public String getStatus() {
            return status;
        }
    }

    class Result {
        Order order;

        public void setOrder(Order order) {
            this.order = order;
        }

        public Order getOrder() {
            return order;
        }
    }

    // public T uu = new T(true);
    // public String uu = "1";

    public static void main(String[] args) {
        int a = 1;
        float b = (float) (a / 0.2f);
        System.out.println(b);

    }

    public boolean some() {
        T t = exchange("123");
        // t.body
        boolean result = (Boolean) t.getBody();
        // t = exchange(r2)
        // result = t.getBody();
        return result;
    }

    public Result getById() {
        Order order = new Order();
        Result result = new Result();
        result.setOrder(order); // order.status
        return result; // result.order.status
    }

    public boolean pay() {
        Result result = getById();
        // result.order.status
        if (result.getOrder().getStatus().equals("CANCEL"))
            // r1 = result.getOrder()
            // r2 = r1.getStatus()
            return true;
        // true
        else
            return false;
        // false
    }

    public T exchange(String url) {
        // T t = new T();
        // r1 = pay();
        // t.init(r1); r1
        // return t; t.body
        return new T(pay());
    }
}
