public class Test {

    class T {
        public Boolean body;

        public T(boolean b) {
            this.body = b;
        }

        public Object getBody() {
            return this.body;
        }
    }

    class Status {
        String status = "CANCEL";

        public String getStatus() {
            return status;
        }
    }

    class Order {
        Status status;

        public Status getStatus() {
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
        boolean result = (Boolean) t.getBody();

        return result;
    }

    public Result getById() {
        Order order = new Order();
        Result result = new Result();
        result.setOrder(order);
        return result;
    }

    public boolean pay() {
        Result result = getById();
        if (result.order.status.status.equals("CANCEL"))
            return true;
        else
            return false;
    }

    public T exchange(String url) {
        return new T(pay());
    }
}
