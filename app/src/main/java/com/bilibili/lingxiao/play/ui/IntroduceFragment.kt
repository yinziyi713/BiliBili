package com.bilibili.lingxiao.play.ui

import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.bilibili.lingxiao.R
import com.bilibili.lingxiao.dagger.DaggerUiComponent
import com.bilibili.lingxiao.home.recommend.model.RecommendData
import com.bilibili.lingxiao.home.recommend.view.RecommendView
import com.bilibili.lingxiao.play.adapter.EndPageAdapter
import com.bilibili.lingxiao.play.adapter.VideoDetailAdapter
import com.bilibili.lingxiao.play.VideoPresenter
import com.bilibili.lingxiao.play.model.CommentData
import com.bilibili.lingxiao.play.model.VideoDetailData
import com.bilibili.lingxiao.play.model.VideoRecoData
import com.bilibili.lingxiao.utils.StringUtil
import com.bilibili.lingxiao.utils.ToastUtil
import com.camera.lingxiao.common.app.BaseFragment
import com.camera.lingxiao.common.utills.PopwindowUtil
import kotlinx.android.synthetic.main.fragment_introduce.*
import kotlinx.android.synthetic.main.fragment_introduce.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class IntroduceFragment :BaseFragment(), RecommendView {
    val TAG = IntroduceFragment::class.java.simpleName
    var mEndPageList = arrayListOf<EndPageData>()
    var mRecommendList = arrayListOf<VideoRecoData.VideoInfo>()
    lateinit var endPageAdapter: EndPageAdapter
    lateinit var videoDetailAdapter: VideoDetailAdapter
    private var videoPresenter = VideoPresenter(this, this)
    override val contentLayoutId: Int
        get() = R.layout.fragment_introduce

    override fun initInject() {
        super.initInject()
        DaggerUiComponent.builder().build().inject(this)
    }
    override fun initWidget(root: View) {
        super.initWidget(root)
        var manager = GridLayoutManager(context,5)
        root.endpage_recycler.layoutManager = manager
        endPageAdapter = EndPageAdapter(R.layout.item_endpage, mEndPageList)
        root.endpage_recycler.adapter = endPageAdapter
        root.endpage_recycler.isNestedScrollingEnabled = false
        //下面的推荐视频
        var recommendManager = LinearLayoutManager(context)
        root.recommend_recycler.layoutManager = recommendManager
        videoDetailAdapter =
            VideoDetailAdapter(R.layout.item_videodetail_recommend, mRecommendList)
        root.recommend_recycler.adapter = videoDetailAdapter
        root.recommend_recycler.isNestedScrollingEnabled = false


        videoDetailAdapter.setOnItemChildClickListener { adapter, view, position ->
            when(view.id){
                R.id.more ->{
                    val popwindowUtil = PopwindowUtil.PopupWindowBuilder(activity!!)
                        .setView(R.layout.pop_comment)
                        .setFocusable(true)
                        .setTouchable(true)
                        .setOutsideTouchable(true)
                        .create()
                    popwindowUtil.showAsDropDown(view,0,-view.getHeight());
                    popwindowUtil.getView<View>(R.id.pop_add_blacklist)!!.setOnClickListener {
                        popwindowUtil.dissmiss()
                    }
                    popwindowUtil.getView<View>(R.id.pop_report)!!.setOnClickListener {
                            v -> popwindowUtil.dissmiss()
                    }
                }
            }
        }
    }

    data class EndPageData(val icon:Drawable,val message:String){

    }

    override fun isRegisterEventBus(): Boolean {
        return true
    }

    /**
     * 粘性事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public fun onGetVideoDetail(data: RecommendData) {
        //针对sticky事件  eventBus会缓存在事件发射队列，
        // 若是订阅关系已经存在则发射出去，但不会销毁。下次再次订阅，会继续接收上一次事件。所以这里接收到了需要移除
        if (endPageAdapter.itemCount >= 5){
            EventBus.getDefault().removeStickyEvent(data)
            return
        }

        var recommend = EndPageData(
            resources.getDrawable(R.drawable.ic_recommend),
            "" + data.like
        )
        var dislike = EndPageData(
            resources.getDrawable(R.drawable.ic_dislike),
            "不喜欢"
        )
        var coin = EndPageData(
            resources.getDrawable(R.drawable.ic_coin),
            "" + data.coin
        )
        var collect = EndPageData(
            resources.getDrawable(R.drawable.ic_collect),
            "" + data.reply
        )
        var share = EndPageData(
            resources.getDrawable(R.drawable.ic_share),
            "" + data.share
        )
        endPageAdapter.addData(recommend)
        endPageAdapter.addData(dislike)
        endPageAdapter.addData(coin)
        endPageAdapter.addData(collect)
        endPageAdapter.addData(share)
        img_head.setImageURI(Uri.parse(data.face))
        username.setText(data.name)
        //var tNames  = data.tname.split("·")
        data.tname?.let {
            type_name.setTitleText(it)
        }
        fensi.text = StringUtil.getBigDecimalNumber(data.reply) + "个粉丝"
        data.title?.let {
            fold_layout.setTitleText(data.title)
        }

        damku_num.text = StringUtil.getBigDecimalNumber(data.danmaku)
        var avNum = StringBuilder()
        avNum.append("   av")
        avNum.append(data.param)
        av_num.text = avNum.toString()
        videoPresenter.getDetailInfo(1,data.param)
        val random = Random()
        val ret = random.nextInt(100)
        videoPresenter.getRecommendList(data.tid,ret)

    }

    override fun onGetRecommendData(recommendData: List<RecommendData>) {

    }

    override fun onGetVideoDetail(data: VideoDetailData) {
        data.description?.let {
            fold_layout.setMessageText(it)
        }
        Log.d(TAG,"设置描述信息${data}")
        play_num.text = StringUtil.getBigDecimalNumber(data.play)
        data.created_at?.let {
            var dataArray = it.split("\\s+")
            if (dataArray.size > 1){
                av_num.text = "  " + dataArray[0] + av_num.text.toString()
            }else{
                av_num.text = "  " + it + av_num.text.toString()
            }
        }

    }

    override fun onGetVideoRecommend(videoRecoData: VideoRecoData) {
        mRecommendList.addAll(videoRecoData.list)
        videoDetailAdapter.notifyDataSetChanged()
    }

    override fun onGetVideoComment(commentData: CommentData) {

    }

    override fun showDialog() {

    }

    override fun diamissDialog() {

    }

    override fun showToast(text: String?) {
        ToastUtil.show(text)
    }
}