package me.leefeng.libverify

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView

/**
 *
 * Created by lilifeng on 2019/4/8
 *
 *
 *
 */
class VerificationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr), TextWatcher, View.OnKeyListener {
    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            backFocus()
        }
        return false
    }

    override fun afterTextChanged(s: Editable?) {
        synchronized(this) {
            focus()
        }

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        getChildAt(etTextCount).layout(l, t, r, b)

    }

    private val density = resources.displayMetrics.density
    private var etTextSize = 18 * density
    private var etTextCount = 4

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.VerificationView)
        etTextSize = array.getDimension(R.styleable.VerificationView_vTextSize, 18 * density)
        etTextCount = array.getInteger(R.styleable.VerificationView_vTextCount, 4)
        array.recycle()

    }

    private val pant = Paint()
    //    val w = context.dimen(if (isPad) R.dimen.pt_100 else R.dimen.pt_70)
//    val middlw = context.dimen(if (isPad) R.dimen.pt_20 else R.dimen.pt_16)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        pant.isAntiAlias = true
        pant.color = Color.parseColor("#DCDCDC")
        pant.strokeWidth = density * 1
        var left = 0f
        val top = sizeH - pant.strokeWidth
//        for (i in 0 until etTextCount) {
//            canvas?.drawLine(left, top, left + w, top, pant)
//            left += w + middlw
//        }

    }

    private fun backFocus() {

        var editText: EditText
        for (i in etTextCount - 1 downTo 0) {
            editText = getChildAt(i) as EditText
            if (editText.text.length == 1) {
                showInputPad(editText)
                editText.setSelection(1)
                return
            }
        }
    }

    /**
     * 展示输入键盘
     */
    private fun showInputPad(editText: EditText) {
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(editText, 0)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        postDelayed({
            focus()
        },200)

    }


    private fun focus() {
        for (i in 0 until etTextCount) {
            val editText = getChildAt(i) as EditText
            if (editText.text.isEmpty()) {
                showInputPad(editText)
                editText.isCursorVisible = true
                return
            }
        }
        if ((getChildAt(etTextCount - 1) as EditText).text.isNotEmpty()) {
            val text = StringBuffer()
            for (i in 0 until etTextCount) {
                text.append((getChildAt(i) as EditText).text.toString())
            }
            finish?.invoke(text.toString())
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                getChildAt(etTextCount - 1).windowToken,
                0
            )
            (getChildAt(etTextCount - 1) as EditText).isCursorVisible = false

        }
    }

    /**
     * 最后一个完成回调
     */
    var finish: ((String) -> Unit)? = null

    init {
        setWillNotDraw(false)
        for (i in 0 until etTextCount) {
            val et = EditText(context)
            et.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            et.gravity = Gravity.CENTER
//            et.background = ColorDrawable(Color.RED)
            et.includeFontPadding = false
//            et.setBackgroundResource(R.drawable.bac_square)
            et.setEms(1)
            et.inputType = InputType.TYPE_CLASS_NUMBER
            et.setTextColor(Color.parseColor("#464646"))
            et.setTextSize(TypedValue.COMPLEX_UNIT_PX, etTextSize)
            et.tag = i
            et.addTextChangedListener(this)
            et.setOnKeyListener(this)
            et.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1))
            addView(et)
        }
        val view = View(context)
        view.setOnClickListener {
            requestEditeFocus()
        }
        addView(view)

    }

    private var sizeH = 0
    private var sizeW = 0
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        sizeW = View.MeasureSpec.getSize(widthMeasureSpec)
        sizeH = View.MeasureSpec.getSize(heightMeasureSpec)


        if (etTextCount > 0) {
            measureChildren(widthMeasureSpec, heightMeasureSpec)
            var left = 0f
            val middle = (sizeW - sizeH * etTextCount) / (etTextCount - 1)
            for (i in 0 until etTextCount) {
                val et = getChildAt(i)
                et.layout(left.toInt(), 0, (left + sizeH).toInt(), sizeH)
//                et.setPadding(0, (sizeH * 0.3).toInt(),0,0)
                left += sizeH + middle
            }
        }
    }

    fun clear() {
        for (j in 0 until etTextCount) {
            val et = (getChildAt(j) as EditText)
            et.removeTextChangedListener(this)
            et.setText("")
            et.addTextChangedListener(this)
        }
        focus()
    }

    /**
     * 主动获取焦点 弹起键盘
     */
    private fun requestEditeFocus() {
        val lastETC = getChildAt(etTextCount - 1) as EditText
        if (lastETC.text.isNotEmpty()) {
            showInputPad(lastETC)
            lastETC.isCursorVisible = true
        } else
            focus()
    }
}