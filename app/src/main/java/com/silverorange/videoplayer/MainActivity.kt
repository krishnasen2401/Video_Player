package com.silverorange.videoplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player
import com.silverorange.videoplayer.Models.VideoDetails
import com.silverorange.videoplayer.Utils.Constants
import com.silverorange.videoplayer.databinding.ActivityMainBinding
import io.noties.markwon.Markwon
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var retrofit: Retrofit
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofit = Retrofit.Builder().baseUrl(Constants().api_url)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val retrofitservice: GetVideos = retrofit.create()
        val call: Call<List<VideoDetails>> = retrofitservice.getVideoList()
        call.enqueue(object : Callback<List<VideoDetails>> {
            override fun onResponse(
                call: Call<List<VideoDetails>>,
                response: Response<List<VideoDetails>>
            ) {
                val videoList: ArrayList<VideoDetails>? = response.body()?.let { ArrayList(it) }
                //sorting in ascending order
                videoList?.sortBy {
                    //simpledateformat for all verison since minsdk is 23
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    format.parse(it.publishedAt)
                }
                val player = ExoPlayer.Builder(this@MainActivity).build()
                //adding to playlist
                if (videoList != null) {
                    for (value in videoList) {
                        //adding video description,title,author as artist to mediadata of the stream
                        val metadata = MediaMetadata.Builder().setDescription(value.description)
                            .setTitle(value.title).setArtist(value.author.name).build()
                        val mediaItem =
                            MediaItem.Builder().setMediaMetadata(metadata).setUri(value.hlsURL)
                                .build()
                        player.addMediaItem(mediaItem)
                    }
                }
                player.addListener(object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        super.onMediaItemTransition(mediaItem, reason)
                        SetControlsVisiblity(player)
                        Log.d(
                            "test",
                            "mediaitem changes item ${player.currentMediaItemIndex} ${mediaItem?.mediaMetadata?.title}"
                        )
                    }
                })
                player.prepare()
                SetControlsVisiblity(player)
                binding.playerView.player = player
            }

            override fun onFailure(call: Call<List<VideoDetails>>, t: Throwable) {
                Log.d("test error", t.toString())
            }
        })
    }

    //setting control visibility,title,author,description based current playlist index
    fun SetControlsVisiblity(player: ExoPlayer) {
        if (player.currentMediaItemIndex == 0) {
            binding.playerView.setShowNextButton(true)
            binding.playerView.setShowPreviousButton(false)
        } else if (player.currentMediaItemIndex == player.mediaItemCount - 1) {
            binding.playerView.setShowNextButton(false)
            binding.playerView.setShowPreviousButton(true)
        } else {
            binding.playerView.setShowNextButton(true)
            binding.playerView.setShowPreviousButton(true)
        }
        val markwon: Markwon = Markwon.create(this@MainActivity);
        binding.videoTitle.text = player.currentMediaItem?.mediaMetadata?.title
        binding.videoAuthor.text = player.currentMediaItem?.mediaMetadata?.artist
        markwon.setMarkdown(
            binding.videoDescription,
            player.currentMediaItem?.mediaMetadata?.description as String
        );
    }

    //pause when in background background play
    override fun onPause() {
        binding.playerView.player?.pause()
        super.onPause()
    }

    interface GetVideos {
        @GET("videos")
        fun getVideoList(): Call<List<VideoDetails>>
    }
}