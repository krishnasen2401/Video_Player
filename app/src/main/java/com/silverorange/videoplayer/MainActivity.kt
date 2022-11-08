package com.silverorange.videoplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.silverorange.videoplayer.Models.VideoDetails
import com.silverorange.videoplayer.Utils.Constants
import com.silverorange.videoplayer.databinding.ActivityMainBinding
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : AppCompatActivity() {
    lateinit var retrofit: Retrofit
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofit = Retrofit.Builder().baseUrl(Constants().api_url)
            .addConverterFactory(GsonConverterFactory.create()).build()
        // val retrofitservice:GetVideos = retrofit.create(GetVideos.class)
        val retrofitservice: GetVideos = retrofit.create()
        var call: Call<List<VideoDetails>> = retrofitservice.getVideoList()
        call.enqueue(object : Callback<List<VideoDetails>> {
            override fun onResponse(
                call: Call<List<VideoDetails>>,
                response: Response<List<VideoDetails>>
            ) {
                val videoList= response.body()?.let { ArrayList(it) }
                Log.d("test", videoList?.get(0)?.description ?:"no output" ) }

            override fun onFailure(call: Call<List<VideoDetails>>, t: Throwable) {
                Log.d("test error",  t.toString() )
            }
        })

    }

    interface GetVideos {
        @GET("videos")
        fun getVideoList(): Call<List<VideoDetails>>
    }
}