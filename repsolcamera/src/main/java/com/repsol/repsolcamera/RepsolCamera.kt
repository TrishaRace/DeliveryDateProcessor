package com.repsol.repsolcamera

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.FragmentActivity
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.*

import com.facebook.drawee.backends.pipeline.Fresco
import com.repsol.repsolcamera.adapters.InstantImageAdapter
import com.repsol.repsolcamera.adapters.MainImageAdapter
import com.repsol.repsolcamera.interfaces.OnSelectionListner
import com.repsol.repsolcamera.modals.Img
import com.repsol.repsolcamera.utility.Constants
import com.repsol.repsolcamera.utility.HeaderItemDecoration
import com.repsol.repsolcamera.utility.ImageFetcher
import com.repsol.repsolcamera.utility.Utility
import com.repsol.repsolcamera.utility.ui.FastScrollStateChangeListener


import java.io.File
import java.util.ArrayList
import java.util.Calendar
import java.util.HashSet

import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.UpdateConfiguration
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.result.BitmapPhoto
import io.fotoapparat.result.WhenDoneListener
import io.fotoapparat.view.CameraView

import io.fotoapparat.result.transformer.scaled
import io.fotoapparat.selector.*
import kotlinx.android.synthetic.main.activity_main_lib.*
import kotlinx.android.synthetic.main.activity_main_lib.view.*

class RepsolCamera : AppCompatActivity(), View.OnTouchListener {
    private val permissionsDelegate = PermissionsDelegate(this)
    private var hasCameraPermission: Boolean = false
    internal var BottomBarHeight = 0
    internal var colorPrimaryDark: Int = 0
    private val handler = Handler()
    private lateinit var mFastScrollStateChangeListener: FastScrollStateChangeListener
    private lateinit var mCamera: CameraView
    private lateinit var recyclerView: RecyclerView
    private lateinit var instantRecyclerView: RecyclerView
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var initaliseadapter: InstantImageAdapter
    private lateinit var mLayoutManager: GridLayoutManager
    private lateinit var status_bar_bg: View
    private lateinit var mScrollbar: View
    private lateinit var topbar: View
    private lateinit var mainFrameLayout: View
    private lateinit var bottomButtons: View
    private lateinit var sendButton: View
    private lateinit var mBubbleView: TextView
    private lateinit var selection_ok: TextView
    private lateinit var img_count: TextView
    private lateinit var tv_longclick_explaining: TextView
    private lateinit var mHandleView: ImageView
    private lateinit var clickme: ImageView
    private lateinit var selection_back: ImageView
    private lateinit var flashBtn: ImageButton
    private var mScrollbarAnimator: ViewPropertyAnimator? = null
    private var mBubbleAnimator: ViewPropertyAnimator? = null
    private lateinit var fl_topbar: FrameLayout
    private val selectionList = HashSet<Img>()
    private val mScrollbarHider = Runnable { hideScrollbar() }
    private lateinit var mainImageAdapter: MainImageAdapter
    private var mViewHeight: Float = 0.toFloat()
    private val mHideScrollbar = true
    private var LongSelection = false
    private var isFlashActivated = false
    private var SelectionCount = 1
    /*int colorPrimary = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme());
    int colorAccent = ResourcesCompat.getColor(getResources(), R.color.colorAccent, getTheme());*/
    private val mScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!mHandleView.isSelected && recyclerView.isEnabled) {
                setViewPositions(getScrollProportion(recyclerView))
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (recyclerView.isEnabled) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        handler.removeCallbacks(mScrollbarHider)
                        Utility.cancelAnimation(mScrollbarAnimator)
                        if (!Utility.isViewVisible(mScrollbar) && recyclerView.computeVerticalScrollRange() - mViewHeight > 0) {
                            mScrollbarAnimator =
                                Utility.showScrollbar(mScrollbar, this@RepsolCamera)
                        }
                    }
                    RecyclerView.SCROLL_STATE_IDLE -> if (mHideScrollbar && !mHandleView.isSelected) {
                        handler.postDelayed(mScrollbarHider, sScrollbarHideDelay.toLong())
                    }
                }
            }
        }
    }
    private var selection_count: TextView? = null
    private val onSelectionListner = object : OnSelectionListner {
        override fun OnClick(img: Img, view: View, position: Int) {
            Log.e("OnClick", "OnClick")
            if (LongSelection) {
                if (selectionList.contains(img)) {
                    selectionList.remove(img)
                    initaliseadapter.Select(false, position)
                    mainImageAdapter.Select(false, position)
                } else {
                    if (SelectionCount <= selectionList.size) {
                        Toast.makeText(
                            this@RepsolCamera,
                            String.format(
                                resources.getString(R.string.selection_limiter_pix),
                                selectionList.size
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    img.position = position
                    selectionList.add(img)
                    initaliseadapter.Select(true, position)
                    mainImageAdapter!!.Select(true, position)
                }
                if (selectionList.size == 0) {
                    LongSelection = false
                    val anim = ScaleAnimation(
                        1f, 0f, // Start and end values for the X axis scaling
                        1f, 0f, // Start and end values for the Y axis scaling
                        Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                        Animation.RELATIVE_TO_SELF, 0.5f
                    ) // Pivot point of Y scaling
                    anim.fillAfter = true // Needed to keep the result of the animation
                    anim.duration = 300
                    anim.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {

                        }

                        override fun onAnimationEnd(animation: Animation) {
                            sendButton.visibility = View.GONE
                            sendButton.clearAnimation()
                        }

                        override fun onAnimationRepeat(animation: Animation) {

                        }
                    })
                    sendButton.startAnimation(anim)

                }
                selection_count!!.text = selectionList.size.toString() + " fotos seleccionadas"
                img_count.text = "" + selectionList.size
            } else {
                img.position = position
                selectionList.add(img)
                returnObjects()
            }
        }

        override fun OnLongClick(img: Img, view: View, position: Int) {
            if (SelectionCount > 1) {
                Log.e("OnLongClick", "OnLongClick")
                LongSelection = true
                if (selectionList.size == 0) {
                    if (mBottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                        sendButton.visibility = View.VISIBLE
                        val anim = ScaleAnimation(
                            0f, 1f, // Start and end values for the X axis scaling
                            0f, 1f, // Start and end values for the Y axis scaling
                            Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                            Animation.RELATIVE_TO_SELF, 0.5f
                        ) // Pivot point of Y scaling
                        anim.fillAfter = true // Needed to keep the result of the animation
                        anim.duration = 300
                        sendButton.startAnimation(anim)
                    }
                    //sendButton.animate().scaleX(1).scaleY(1).setDuration(800).start();
                }
                if (selectionList.contains(img)) {
                    selectionList.remove(img)
                    initaliseadapter.Select(false, position)
                    mainImageAdapter.Select(false, position)
                } else {
                    img.position = position
                    selectionList.add(img)
                    initaliseadapter.Select(true, position)
                    mainImageAdapter.Select(true, position)
                }
                topbar.setBackgroundColor(colorPrimaryDark)
                selection_count!!.text = selectionList.size.toString() + " fotos seleccionadas"
                img_count.text = "" + selectionList.size
            }

        }
    }

    private var fotoapparat: Fotoapparat? = null

    public override fun onStart() {
        super.onStart()
        if (fotoapparat != null)
            fotoapparat!!.start()
    }

    public override fun onStop() {
        super.onStop()
        if (fotoapparat != null)
            fotoapparat!!.stop()
    }


    private fun hideScrollbar() {
        val transX =
            resources.getDimensionPixelSize(R.dimen.fastscroll_scrollbar_padding_end).toFloat()
        mScrollbarAnimator = mScrollbar.animate().translationX(transX).alpha(0f)
            .setDuration(Constants.sScrollbarAnimDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    mScrollbar.visibility = View.GONE
                    mScrollbarAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    mScrollbar.visibility = View.GONE
                    mScrollbarAnimator = null
                }
            })
    }

    fun returnObjects() {
        val list = ArrayList<String>()
        for (i in selectionList) {
            list.add(i.url)
        }
        val resultIntent = Intent()
        resultIntent.putStringArrayListExtra(IMAGE_RESULTS, list)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun toggleFlashAction() {
        isFlashActivated = !isFlashActivated
        if (isFlashActivated) {
            flashBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_flash_on_black_24dp))
        } else {
            flashBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_flash_off_black_24dp))
        }
        fotoapparat?.updateConfiguration(
            UpdateConfiguration(
                flashMode = if ((isFlashActivated)) torch() else off()

            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.SetupStatusBarHiden(this)
        Utility.hideStatusBar(this)

        setContentView(R.layout.activity_main_lib)
        Fresco.initialize(this)
        initialize()

        hasCameraPermission = permissionsDelegate.hasCameraPermission()
        mCamera = camera_view
        if (hasCameraPermission) {

            fotoapparat = Fotoapparat
                .with(this)
                .into(mCamera)           // view which will draw the camera preview
                .previewScaleType(ScaleType.CenterCrop)  // we want the preview to fill the view
                .photoResolution(highestResolution())   // we want to have the biggest photo possible
                .lensPosition(back())       // we want back camera
                .flash(off())//        .flashBtn(if(isFlashActivated)FlashSelectorsKt.off() else FlashSelectorsKt.off())
                .focusMode(
                    firstAvailable(  // (optional) use the first focus mode which is supported by device
                        continuousFocusPicture(),
                        autoFocus(), // in case if continuous focus is not available on device, auto focus will be used
                        fixed()             // if even auto focus is not available - fixed focus mode will be used
                    )
                )
                .build()
            fotoapparat!!.start()
        } else {
            permissionsDelegate.requestCameraPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            hasCameraPermission = true
            fotoapparat!!.start()
        } else {
            finish()
        }
    }

    private fun initialize() {
        Utility.getScreensize(this)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        try {
            SelectionCount = intent.getIntExtra(SELECTION, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        colorPrimaryDark = ResourcesCompat.getColor(resources, R.color.orangeRepsol, theme)
        clickme = findViewById(R.id.clickme) as ImageView
        flashBtn = findViewById(R.id.bFlash) as ImageButton
        topbar = findViewById(R.id.topbar) as View
        selection_count = findViewById(R.id.selection_count) as TextView
        selection_ok = findViewById(R.id.selection_ok) as TextView
        selection_back =findViewById(R.id.selection_back) as ImageView
        sendButton = findViewById(R.id.sendButton) as FrameLayout
        img_count = findViewById(R.id.img_count) as TextView
        mBubbleView = findViewById(R.id.fastscroll_bubble) as TextView
        mHandleView = findViewById(R.id.fastscroll_handle) as ImageView
        mScrollbar = findViewById(R.id.fastscroll_scrollbar) as View
        mScrollbar.visibility = View.GONE
        mBubbleView.visibility = View.GONE
        fl_topbar = findViewById(R.id.fl_topbar) as FrameLayout
        tv_longclick_explaining = findViewById(R.id.tv_longclick_explaining) as TextView
            if (SelectionCount > 1) {
            tv_longclick_explaining.text =
                "Recuerda que puedes seleccionar mas de una foto sí las mantienes pulsadas"
        } else {
            val lp = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.dimen_topbar_only_one_element_height)
            )
            topbar.layoutParams = lp
        }
        bottomButtons = findViewById(R.id.bottomButtons) as View
        TOPBAR_HEIGHT = Utility.convertDpToPixel(56f, this@RepsolCamera)
        status_bar_bg = findViewById(R.id.status_bar_bg) as View
        instantRecyclerView = findViewById(R.id.instantRecyclerView) as RecyclerView
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        instantRecyclerView.layoutManager = linearLayoutManager
        initaliseadapter = InstantImageAdapter(this)
        initaliseadapter.AddOnSelectionListner(onSelectionListner)
        instantRecyclerView.adapter = initaliseadapter
        recyclerView = findViewById(R.id.recyclerView) as RecyclerView
        recyclerView.addOnScrollListener(mScrollListener)
        mainFrameLayout = findViewById(R.id.mainFrameLayout)
        BottomBarHeight = Utility.getSoftButtonsBarSizePort(this)
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        lp.setMargins(0, 0, 0, BottomBarHeight)
        mainFrameLayout.layoutParams = lp
        val fllp = sendButton.layoutParams as FrameLayout.LayoutParams
        fllp.setMargins(
            0, 0, Utility.convertDpToPixel(16f, this).toInt(),
            Utility.convertDpToPixel(174f, this).toInt()
        )
        sendButton.layoutParams = fllp
        mainImageAdapter = MainImageAdapter(this)
        mLayoutManager = GridLayoutManager(this, MainImageAdapter.spanCount)
        mLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                when (mainImageAdapter.getItemViewType(position)) {
                    MainImageAdapter.HEADER -> return MainImageAdapter.spanCount
                    MainImageAdapter.ITEM -> return 1
                    else -> return 1
                }
            }
        }

        recyclerView.layoutManager = mLayoutManager
        mainImageAdapter.AddOnSelectionListner(onSelectionListner)
        recyclerView.adapter = mainImageAdapter
        recyclerView.addItemDecoration(
            HeaderItemDecoration(
                this,
                recyclerView,
                mainImageAdapter!!
            )
        )
        mHandleView.setOnTouchListener(this)
        flashBtn.setOnClickListener { toggleFlashAction() }
        clickme.setOnClickListener { takePhotoPicture() }

        selection_ok.setOnClickListener {
            // Toast.makeText(RepsolCamera.this, "fin", Toast.LENGTH_SHORT).show();
            //Log.e("Hello", "onclick");
            returnObjects()
        }
        sendButton.setOnClickListener {
            //Toast.makeText(RepsolCamera.this, "fin", Toast.LENGTH_SHORT).show();
            //Log.e("Hello", "onclick");
            returnObjects()
        }
        selection_back.setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        updateImages()
    }

    // TODO - TAKE PHOTO
    private fun takePhotoPicture() {
        val photoResult = fotoapparat!!.takePicture()
        photoResult.toBitmap(scaled(0.25f))
            .whenDone(object : WhenDoneListener<BitmapPhoto> {
                override fun whenDone(bitmapPhoto: BitmapPhoto?) {
                    if (bitmapPhoto == null) {
                        Log.e("FotoCapture", "Couldn't capture photo.")
                        return
                    }

                    val photo = Utility.writeImage(bitmapPhoto)
                    selectionList.clear()
                    selectionList.add(Img("", "", photo.absolutePath, ""))
                    returnObjects()
                }
            })
    }

    private fun updateImages() {
        mainImageAdapter.ClearList()
        val cursor: Cursor = Utility.getCursor(this@RepsolCamera)!!
        val INSTANTLIST = ArrayList<Img>()
        var header = ""
        var limit = 100
        if (cursor.count < 100) {
            limit = cursor.count
        }
        val date = cursor.getColumnIndex(MediaStore.Images.Media.
            DATE_TAKEN)
        val data = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
        val contenturl = cursor.getColumnIndex(MediaStore.Images.Media._ID)
        for (i in 0 until limit) {
            cursor.moveToNext()
            val path = Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "" + cursor.getInt(contenturl)
            )
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = cursor.getLong(date)
            val mydate = Utility.getDateDifference(calendar)
            if (!header.equals("" + mydate, ignoreCase = true)) {
                header = "" + mydate
                INSTANTLIST.add(Img("" + mydate, "", "", ""))
            }
            INSTANTLIST.add(Img("" + header, "" + path, cursor.getString(data), ""))
        }
        cursor.close()
        object : ImageFetcher() {
            override fun onPostExecute(imgs: ArrayList<Img>) {
                super.onPostExecute(imgs)
                mainImageAdapter!!.addImageList(imgs)
            }
        }.execute(Utility.getCursor(this@RepsolCamera))
        initaliseadapter.addImageList(INSTANTLIST)
        mainImageAdapter.addImageList(INSTANTLIST)
        setBottomSheetBehavior()
    }

    private fun setBottomSheetBehavior() {
        val bottomSheet = findViewById(R.id.bottom_sheet) as FrameLayout
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        mBottomSheetBehavior.peekHeight = Utility.convertDpToPixel(194f, this).toInt()
        mBottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Utility.manupulateVisibility(
                    this@RepsolCamera, slideOffset,
                    instantRecyclerView, recyclerView, status_bar_bg,
                    topbar, bottomButtons, sendButton, LongSelection
                )
                if (slideOffset == 1f) {
                    Utility.showScrollbar(mScrollbar, this@RepsolCamera)
                    mainImageAdapter.notifyDataSetChanged()
                    mViewHeight = mScrollbar.measuredHeight.toFloat()
                    handler.post { setViewPositions(getScrollProportion(recyclerView!!)) }
                    sendButton.visibility = View.GONE
                } else if (slideOffset == 0f) {

                    initaliseadapter.notifyDataSetChanged()
                    hideScrollbar()
                    img_count.text = "" + selectionList.size
                }
            }
        })
    }

    //
    private fun getScrollProportion(recyclerView: RecyclerView): Float {
        val verticalScrollOffset = recyclerView.computeVerticalScrollOffset()
        val verticalScrollRange = recyclerView.computeVerticalScrollRange()
        val rangeDiff = verticalScrollRange - mViewHeight
        val proportion = verticalScrollOffset.toFloat() / if (rangeDiff > 0) rangeDiff else 1f
        return mViewHeight * proportion
    }

    private fun setViewPositions(y: Float) {
        val handleY = Utility.getValueInRange(
            0,
            (mViewHeight - mHandleView.height).toInt(),
            (y - mHandleView.height / 2).toInt()
        )
        mBubbleView.y = handleY + Utility.convertDpToPixel(56.toFloat(), this@RepsolCamera)
        mHandleView.y = handleY.toFloat()
    }


    private fun setRecyclerViewPosition(y: Float) {
        if (recyclerView.adapter != null) {
            val itemCount = recyclerView.adapter!!.itemCount
            val proportion: Float

            if (mHandleView.y == 0f) {
                proportion = 0f
            } else if (mHandleView.y + mHandleView.height >= mViewHeight - sTrackSnapRange) {
                proportion = 1f
            } else {
                proportion = y / mViewHeight
            }

            val scrolledItemCount = Math.round(proportion * itemCount)
            val targetPos = Utility.getValueInRange(0, itemCount - 1, scrolledItemCount)
            recyclerView.layoutManager!!.scrollToPosition(targetPos)

            mBubbleView.text = mainImageAdapter.getSectionMonthYearText(targetPos)
        }
    }

    private fun showBubble() {
        if (!Utility.isViewVisible(mBubbleView)) {
            mBubbleView.visibility = View.VISIBLE
            mBubbleView.alpha = 0f
            mBubbleAnimator = mBubbleView.animate().alpha(1f)
                .setDuration(sBubbleAnimDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {

                    // adapter required for new alpha value to stick
                })
            mBubbleAnimator!!.start()
        }
    }

    private fun hideBubble() {
        if (Utility.isViewVisible(mBubbleView)) {
            mBubbleAnimator = mBubbleView.animate().alpha(0f)
                .setDuration(sBubbleAnimDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        mBubbleView.visibility = View.GONE
                        mBubbleAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        super.onAnimationCancel(animation)
                        mBubbleView.visibility = View.GONE
                        mBubbleAnimator = null
                    }
                })
            mBubbleAnimator!!.start()
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.x < mHandleView.x - ViewCompat.getPaddingStart(mHandleView)) {
                    return false
                }
                mHandleView.isSelected = true
                handler.removeCallbacks(mScrollbarHider)
                Utility.cancelAnimation(mScrollbarAnimator)
                Utility.cancelAnimation(mBubbleAnimator)

                if (!Utility.isViewVisible(mScrollbar) && recyclerView.computeVerticalScrollRange() - mViewHeight > 0) {
                    mScrollbarAnimator = Utility.showScrollbar(mScrollbar, this@RepsolCamera)
                }

                if (mainImageAdapter != null) {
                    showBubble()
                }

                mFastScrollStateChangeListener.onFastScrollStart(this)
                val y = event.rawY
                mBubbleView.text =
                    mainImageAdapter.getSectionText(recyclerView.verticalScrollbarPosition)
                setViewPositions(y - TOPBAR_HEIGHT)
                setRecyclerViewPosition(y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val y = event.rawY
                mBubbleView.text =
                    mainImageAdapter.getSectionText(recyclerView.verticalScrollbarPosition)
                setViewPositions(y - TOPBAR_HEIGHT)
                setRecyclerViewPosition(y)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mHandleView.isSelected = false
                if (mHideScrollbar) {
                    handler.postDelayed(mScrollbarHider, sScrollbarHideDelay.toLong())
                }
                hideBubble()
                mFastScrollStateChangeListener.onFastScrollStop(this)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onBackPressed() {
        if (selectionList.size > 0) {
            for (img in selectionList) {
                mainImageAdapter.itemList[img.position].selected = false
                mainImageAdapter.notifyItemChanged(img.position)
                initaliseadapter.itemList[img.position].selected = false
                initaliseadapter.notifyItemChanged(img.position)
            }
            LongSelection = false
            val anim = ScaleAnimation(
                1f, 0f, // Start and end values for the X axis scaling
                1f, 0f, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f
            ) // Pivot point of Y scaling
            anim.fillAfter = true // Needed to keep the result of the animation
            anim.duration = 300
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    sendButton.visibility = View.GONE
                    sendButton.clearAnimation()
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            sendButton.startAnimation(anim)
            selectionList.clear()
        } else if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        } else {
            super.onBackPressed()
        }
    }

    companion object {

        private val sBubbleAnimDuration = 1000
        private val sScrollbarHideDelay = 1000
        private val SELECTION = "selection"
        private val sTrackSnapRange = 5
        var IMAGE_RESULTS = "image_results"
        var TOPBAR_HEIGHT: Float = 0.toFloat()

        fun start(context: Activity, requestCode: Int) {
            val i = Intent(context, RepsolCamera::class.java)
            context.startActivityForResult(i, requestCode)
        }

        fun start(context: FragmentActivity, requestCode: Int, selectionCount: Int) {
            val i = Intent(context, RepsolCamera::class.java)
            i.putExtra(SELECTION, selectionCount)
            context.startActivityForResult(i, requestCode)
        }

        @JvmOverloads
        fun start(context: Fragment, requestCode: Int, selectionCount: Int = 1) {
            val i = Intent(context.activity, RepsolCamera::class.java)
            i.putExtra(SELECTION, selectionCount)
            context.startActivityForResult(i, requestCode)
        }
    }

}