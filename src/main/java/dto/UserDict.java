package dto;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import static constants.Constants.USERINFOSIZE;

public class UserDict {
    Map<Integer, UserInfo> dict;

    public UserDict() {
        this.dict = new ConcurrentHashMap<>();
    }

    public void updateDict(Swipe swipe){
        UserInfo swiper;
        if(dict.containsKey(swipe.getSwiper())){
             swiper = dict.get(swipe.getSwiper());
        } else {
            dict.put(swipe.getSwiper(), new UserInfo(swipe.getSwiper(), USERINFOSIZE));
            swiper = dict.get(swipe.getSwiper());
        }

        if(swipe.getRightOrNot()){
            swiper.addLikeUsers(swipe.getSwipee());
        } else {
            swiper.addDislikeNum();
        }
    }

    public Queue<Integer> getUserQueue(int userId){
        return this.dict.get(userId).getLikeQueue();
    }
}
