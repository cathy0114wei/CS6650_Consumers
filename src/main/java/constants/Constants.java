package constants;

public class Constants {
    public static int CONSUMER_THREAD_NUM = 100;
    public static String QUEUE_NAME = "TempStore";
    public static String DB_NAME = "SwipeData";
    // The consumer only need to do post, so we have to update both SwipeF and SwipeR
    public static String DB_ER2EE = "SwipeF";
    public static String DB_EE2ER = "SwipeR";
    public static String DB_DIS = "SwipeF_Dislike";

    public static int QUEUE_NUM = 0;
    public static int USERINFOSIZE = 100;
    public static int CHANNEL_POOL_CAPACITY = 50;
    //public RMQ ip
    public static String HOSTNAME = "54.226.25.251";

    //private RMQ ip
//    public static String HOSTNAME = "172.31.22.115";

    public static String USERNAME = "test";
    public static String PASSWORD = "test";
    //DB access
    public static String accessKeyId = "ASIA3MRIZ5REKFO5YRMB";
    public static String secretAccessKey = "pfrbzB0/J6RmW9I3gPg99y0XAkBZtXOHITJvYDAc";
    public static String sessionToken = "FwoGZXIvYXdzEOv//////////wEaDHwR7ZEXNOjtTFOTGyLIAfpb8lvUtoZLxHwQXNYtGEX+eB2kapHrczDDYn/6KaG7LtwpmPlGi9IN7bjt2B7QEtxeiqmem2HQ0EP45DfMfv/KrkGSnGYAtHVnp4Y5LsXoMyO49Fq+kZnmre0Q+qmtKF2/1BGjWTQeGJrY34x/4GmHktQcxzqRw+0gAhSr9O5Li9kUhC6CENgqFG5KSU672hBBVeEpV1AHI332LndG2jsOvVTvyqG6glm0dTUgLsoWet4tgfRM1JthKsuA+5m5L0fGK0MduSwVKPiyo6EGMi2BJTkVlPL+B3YTZ1WiDsKCp29adGmBxsWsbkQWkB2EnRpY4yfPg1N1ER0wWOI=";
}
