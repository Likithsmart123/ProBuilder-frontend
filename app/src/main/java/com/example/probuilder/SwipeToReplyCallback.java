package com.example.probuilder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeToReplyCallback extends ItemTouchHelper.Callback {

    private final Context context;
    private final ChatAdapter.OnReplyListener replyListener;
    private final ChatAdapter adapter;
    private final Drawable replyIcon;
    private final Drawable replyBackground;
    
    private boolean swipeBack = false;
    private boolean isSwipeComplete = false;

    public SwipeToReplyCallback(Context context, ChatAdapter adapter, ChatAdapter.OnReplyListener listener) {
        this.context = context;
        this.adapter = adapter;
        this.replyListener = listener;
        this.replyIcon = ContextCompat.getDrawable(context, R.drawable.ic_reply);
        this.replyBackground = ContextCompat.getDrawable(context, R.drawable.bg_reply_icon);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Only allow swiping from left to right (like WhatsApp)
        return makeMovementFlags(0, ItemTouchHelper.RIGHT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Will be handle in clearView so we can animate it back automatically
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void setTouchListener(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener((v, event) -> {
            swipeBack = event.getAction() == android.view.MotionEvent.ACTION_CANCEL || event.getAction() == android.view.MotionEvent.ACTION_UP;
            if (swipeBack) {
                if (Math.abs(dX) > 200) { // Threshold to trigger reply
                    isSwipeComplete = true;
                }
            }
            return false;
        });
        
        // Draw the Reply Icon behind the swiped item
        View itemView = viewHolder.itemView;
        int iconMargin = (itemView.getHeight() - replyIcon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + iconMargin;
        int iconBottom = iconTop + replyIcon.getIntrinsicHeight();
        
        int iconLeft = itemView.getLeft() + 50; // Padding from left edge
        int iconRight = iconLeft + replyIcon.getIntrinsicWidth();

        // Reveal the icon progressively based on swipe distance
        if (dX > 0) {
            float progress = Math.min(dX / 200f, 1f); 
            int alpha = (int) (progress * 255);
            
            if (replyBackground != null) {
                replyBackground.setBounds(iconLeft - 16, iconTop - 16, iconRight + 16, iconBottom + 16);
                replyBackground.setAlpha(alpha);
                replyBackground.draw(c);
            }
            
            if (replyIcon != null) {
                replyIcon.setAlpha(alpha);
                replyIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                replyIcon.draw(c);
            }
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        
        if (isSwipeComplete) {
            isSwipeComplete = false;
            int position = viewHolder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && replyListener != null) {
                // Get the message and trigger reply
                replyListener.onReply(adapter.getMessage(position));
            }
        }
    }
}
