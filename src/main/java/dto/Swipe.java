package dto;

import lombok.Data;

@Data
public class Swipe {
    private Integer swiper;
    private Integer swipee;
    private String comment;
    /*
    * stands for like or not
    * */
    private Boolean rightOrNot;
}
