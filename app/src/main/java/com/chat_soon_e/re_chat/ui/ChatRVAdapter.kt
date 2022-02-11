package com.chat_soon_e.re_chat.ui

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.util.SparseBooleanArray
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.chat_soon_e.re_chat.ApplicationClass.Companion.dateToString
import com.chat_soon_e.re_chat.ApplicationClass.Companion.loadBitmap
import com.chat_soon_e.re_chat.R
import com.chat_soon_e.re_chat.data.entities.*
import com.chat_soon_e.re_chat.data.local.AppDatabase
import com.chat_soon_e.re_chat.databinding.ItemChatBinding
import com.chat_soon_e.re_chat.databinding.ItemChatChooseBinding

class ChatRVAdapter(private val mContext: ChatActivity, private val mItemClickListener: MyItemClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var chatList = ArrayList<ChatList>()
    var selectedItemList: SparseBooleanArray = SparseBooleanArray(0)
    private lateinit var popup: PopupMenu
    private lateinit var binding: ItemChatBinding
    private var currentPosition: Int = 0

    // 클릭 인터페이스
    interface MyItemClickListener {
        fun onRemoveChat(position: Int)
        fun onDefaultChatLongClick(popupMenu: PopupMenu)
        fun onChooseChatClick(view: View, position: Int)
    }

    // 뷰홀더를 생성해줘야 할 때 호출되는 함수로, 아이템 뷰 객체를 만들어서 뷰 홀더에 던져준다.
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ChatViewType.CHOOSE -> {
                ChooseViewHolder(ItemChatChooseBinding.inflate(
                    LayoutInflater.from(viewGroup.context), viewGroup, false),
                    mItemClickListener = mItemClickListener)
            }
            else -> {
                DefaultViewHolder(ItemChatBinding.inflate(
                    LayoutInflater.from(viewGroup.context), viewGroup, false))
            }
        }
    }

    // 뷰홀더에 데이터 바인딩을 해줘야 할 때마다 호출되는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(chatList[position].viewType) {
            ChatListViewType.CHOOSE -> {
                (holder as ChooseViewHolder).bind(chatList[position])
                (holder as ChooseViewHolder).itemView.isSelected = isItemSelected(position)
            }
            else -> {
                (holder as DefaultViewHolder).bind(chatList[position])
                (holder as DefaultViewHolder).itemView.isSelected = isItemSelected(position)
            }
        }
    }

    override fun getItemCount(): Int = chatList.size

    @SuppressLint("NotifyDataSetChanged")
    private fun removeChat(position: Int) {
        //맨 위에 있는 position을 선택했을 때
//        chatList.removeAt(position)
        if(position==0){
            Log.d("ChatPosition", "ItemCount ${chatList.size}")
            Log.d("ChatPosition", "Items ${chatList.toString()}")
        }
        notifyDataSetChanged()
//
//        notifyItemRangeChanged(position, itemCount);
//        notifyItemRemoved(position)

    }

    //AddData
    @SuppressLint("NotifyDataSetChanged")
    fun addItem(chat: List<ChatList>){
        chatList.clear()
        chatList.addAll(chat as ArrayList)

        notifyDataSetChanged()
    }

    // will toggle the selection of items
    private fun toggleItemSelected(view: View?, position: Int) {
        if(selectedItemList.get(position, false)) {
            selectedItemList.delete(position)
        } else {
            selectedItemList.put(position, true)
        }
        notifyItemChanged(position)
    }

    // selectedItemList 초기화
    @SuppressLint("NotifyDataSetChanged")
    fun clearSelectedItemList() {
        selectedItemList.clear()
        notifyDataSetChanged()
    }

    //선택된 chatIdx 가져오기
    fun getSelectedItemList():List<Long>{
        val TG="moveList"
        var chatIdxList=ArrayList<Long>()
        val selectedList=chatList.filter{ chatlist-> chatlist.isChecked as Boolean }

        for(i in selectedList){
            chatIdxList.add(i.chatIdx)
        }

        return chatIdxList
    }

    // 뷰타입 설정
    @SuppressLint("NotifyDataSetChanged")
    fun setViewType(currentMode: Int) {
        val newChatList = ArrayList<ChatList>()
        for(i in 0 until chatList.size) {
            if(currentMode == 0) {
                // 일반 모드
                chatList[i].viewType = ChatListViewType.DEFAULT
            } else {
                // 선택 모드
                chatList[i].viewType = ChatListViewType.CHOOSE
            }
            newChatList.add(chatList[i])
        }
        this.chatList = newChatList
        notifyDataSetChanged()
    }

    fun setChecked(position: Int) {
        chatList[position].isChecked = !chatList[position].isChecked!!
        notifyItemChanged(position)
    }

    // 아이템뷰가 선택되었는지를 알려주는 함수
    private fun isItemSelected(position: Int): Boolean {
        return selectedItemList.get(position, false)
    }

    // 직접 설정한 뷰타입으로 설정되게 만든다.
    override fun getItemViewType(position: Int): Int = chatList[position].viewType!!

    // 디폴트 뷰홀더
    inner class DefaultViewHolder(private val binding: ItemChatBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.itemChatDefaultMessageTv.setOnLongClickListener {
                toggleItemSelected(itemView, position = bindingAdapterPosition)
                popup = PopupMenu(mContext, binding.itemChatDefaultMessageTv, Gravity.START, 0, R.style.MyFolderBottomPopupMenuTheme)
                popup.menuInflater.inflate(R.menu.popup_chat_option_menu, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    when(item?.itemId) {
                        R.id.popup_chat_option_menu_delete -> {
                            // 삭제하기
                            Log.d("ChatPosition", "position: $bindingAdapterPosition")
                            mItemClickListener.onRemoveChat(bindingAdapterPosition)
                            //position을 얻어올떄 오류 발생
                            AppDatabase.getInstance(mContext)!!.chatDao().deleteByChatIdx(chatList[bindingAdapterPosition].chatIdx)
                            removeChat(bindingAdapterPosition)
                        }
                    }
                    false
                }
                mItemClickListener.onDefaultChatLongClick(popup)
                return@setOnLongClickListener false
            }
        }


//            // 날짜가 바뀐 걸 확인을 하면
//            binding.itemChatDefaultDateTimeLayout.visibility = View.VISIBLE
//            binding.itemChatDefaultNewDateTimeTv.text = // 년월일

//            if(position == 0) // 날짜 표시
//                else if(position != 0 && 이전 포지션에 들어있는 chatList의 데이트타임과 비교해서 1일 이상 차이가 나면 ture 반환) // 날짜 표시
//            else //무시
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(chat: ChatList) {
            Log.d("chatNameRVA", chat.nickName.toString())
            binding.itemChatDefaultNameTv.text = chat.nickName
            binding.itemChatDefaultMessageTv.text = chat.message
//            binding.itemChatDefaultDateTimeTv.text = dateToString(chat.postTime!!)
            binding.itemChatDefaultDateTimeTv.text = chat.postTime
            binding.itemChatDefaultProfileIv.setImageBitmap(loadBitmap(chat.profileImg!!, mContext))

        }
    }

    // 선택 모드 뷰홀더
    inner class ChooseViewHolder(private val binding: ItemChatChooseBinding, private val mItemClickListener: MyItemClickListener)
        : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.itemChatChooseMessageTv.setOnClickListener {
                toggleItemSelected(itemView, position = bindingAdapterPosition)
                mItemClickListener.onChooseChatClick(itemView, position = bindingAdapterPosition)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(chat: ChatList) {
            Log.d("chatNameRVA", chat.nickName.toString())
            binding.itemChatChooseNameTv.text = chat.nickName
            binding.itemChatChooseMessageTv.text = chat.message
//            binding.itemChatChooseDateTimeTv.text = dateToString(chat.postTime!!)
            binding.itemChatChooseDateTimeTv.text = chat.postTime
            binding.itemChatChooseProfileIv.setImageBitmap(loadBitmap(chat.profileImg!!, mContext))
        }
    }

}