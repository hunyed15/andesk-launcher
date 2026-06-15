package com.andesk.launcher.ui.home

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

/**
 * 拖拽帮助类 - 处理应用排序和文件夹创建
 */
class DragHelper(
    private val adapter: DesktopGridAdapter,
    private val viewPager: ViewPager2? = null,
    private val onDragEnd: (fromPos: Int, toPos: Int) -> Unit,
    private val onMergeToFolder: (fromPos: Int, toPos: Int) -> Unit
) : ItemTouchHelper.Callback() {

    private var isDragging = false
    private var dragStartPos = RecyclerView.NO_POSITION
    private var mergeTargetPos = RecyclerView.NO_POSITION

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPos = viewHolder.bindingAdapterPosition
        val toPos = target.bindingAdapterPosition
        if (fromPos == RecyclerView.NO_POSITION || toPos == RecyclerView.NO_POSITION) return false
        
        // 通知适配器移动项
        adapter.onItemMove(fromPos, toPos)
        
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // 不支持滑动删除
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> {
                isDragging = true
                dragStartPos = viewHolder?.bindingAdapterPosition ?: RecyclerView.NO_POSITION
                // 禁用ViewPager2滑动
                viewPager?.isUserInputEnabled = false
                // 放大被拖拽的项
                viewHolder?.itemView?.apply {
                    scaleX = 1.1f
                    scaleY = 1.1f
                    alpha = 0.8f
                }
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        
        // 恢复原始大小
        viewHolder.itemView.apply {
            scaleX = 1.0f
            scaleY = 1.0f
            alpha = 1.0f
        }
        
        // 恢复ViewPager2滑动
        viewPager?.isUserInputEnabled = true
        
        if (isDragging) {
            isDragging = false
            val endPos = viewHolder.bindingAdapterPosition
            resetChildAlpha(recyclerView)

            if (
                mergeTargetPos != RecyclerView.NO_POSITION &&
                endPos != RecyclerView.NO_POSITION &&
                mergeTargetPos != endPos
            ) {
                onMergeToFolder(endPos, mergeTargetPos)
            } else if (dragStartPos != RecyclerView.NO_POSITION && endPos != RecyclerView.NO_POSITION && dragStartPos != endPos) {
                onDragEnd(dragStartPos, endPos)
            }
            dragStartPos = RecyclerView.NO_POSITION
            mergeTargetPos = RecyclerView.NO_POSITION
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        
        // 检测是否拖拽到另一个项上
        if (isDragging && isCurrentlyActive) {
            val draggedView = viewHolder.itemView
            val draggedCenterX = draggedView.left + draggedView.width / 2 + dX
            val draggedCenterY = draggedView.top + draggedView.height / 2 + dY
            var closestMergeTarget = RecyclerView.NO_POSITION
            
            // 检查是否重叠
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                if (child != draggedView) {
                    val childCenterX = child.left + child.width / 2
                    val childCenterY = child.top + child.height / 2
                    
                    val distance = Math.sqrt(
                        Math.pow((draggedCenterX - childCenterX).toDouble(), 2.0) +
                        Math.pow((draggedCenterY - childCenterY).toDouble(), 2.0)
                    )
                    
                    // 如果距离小于阈值，可以创建文件夹
                    if (distance < child.width * 0.5) {
                        // 高亮目标项
                        child.alpha = 0.5f
                        closestMergeTarget = recyclerView.getChildViewHolder(child).bindingAdapterPosition
                    } else {
                        child.alpha = 1.0f
                    }
                }
            }
            mergeTargetPos = closestMergeTarget
        }
    }

    private fun resetChildAlpha(recyclerView: RecyclerView) {
        for (i in 0 until recyclerView.childCount) {
            recyclerView.getChildAt(i).alpha = 1.0f
        }
    }
}
