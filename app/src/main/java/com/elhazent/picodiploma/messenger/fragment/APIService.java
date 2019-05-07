package com.elhazent.picodiploma.messenger.fragment;

import com.elhazent.picodiploma.messenger.notification.MyResponse;
import com.elhazent.picodiploma.messenger.notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAj_U9-H4:APA91bFljMCajXKAcDQhHu-6HB1bWG58g_dcdlHUJ1kfAytErefnZbNWea7LoOh4ZUOZoZLeyuk7Tl_o_q-oLXucF1R2fipzVrH2PYbK34tlerwjukgoum1NifumZE9JS2yX_58Ax2eW"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
