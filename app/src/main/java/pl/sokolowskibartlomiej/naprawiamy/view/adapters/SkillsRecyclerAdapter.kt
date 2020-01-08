package pl.sokolowskibartlomiej.naprawiamy.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_skill.view.*
import kotlinx.android.synthetic.main.item_skills_header.view.*
import pl.sokolowskibartlomiej.naprawiamy.R
import pl.sokolowskibartlomiej.naprawiamy.model.Category
import pl.sokolowskibartlomiej.naprawiamy.model.Skill
import pl.sokolowskibartlomiej.naprawiamy.view.ui.SectionHeader

class SkillsRecyclerAdapter : ListAdapter<Any, RecyclerView.ViewHolder>(SkillDiff) {

    companion object {
        private const val VIEW_TYPE_HEADING = R.layout.item_skills_header
        private const val VIEW_TYPE_SKILL = R.layout.item_skill

        private fun insertCategoryHeadings(list: List<Category>?): List<Any> {
            val newList = mutableListOf<Any>()
            list?.forEach { category ->
                if (category.parentCategoryId == null) {
                    newList.add(SectionHeader(category))
                } else {
                    newList.add(Skill(category))
                }
            }
            return newList
        }
    }

    private var selectedCategories = arrayListOf<Int>()
    private var isClickable = true
    private var isMultipleCategoriesAllowed = false

    fun submitSkillsList(list: List<Category>?) {
        super.submitList(insertCategoryHeadings(list))
    }

    fun getSelectedCategoryId() = if (selectedCategories.size > 0) selectedCategories[0] else null
    fun setSelectedCategoryId(categoryId: Int) {
        if (categoryId != 0) {
            selectedCategories = arrayListOf(categoryId)
            notifyDataSetChanged()
        }
    }

    fun getSelectedCategories() = selectedCategories
    fun setSelectedCategories(categoriesIds: ArrayList<Int>) {
        selectedCategories = categoriesIds
        notifyDataSetChanged()
    }

    fun setIsClickable(newValue: Boolean) {
        isClickable = newValue
        notifyDataSetChanged()
    }

    fun setIsMultipleCategoriesAllowed(newValue: Boolean) {
        isMultipleCategoriesAllowed = newValue
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SectionHeader -> VIEW_TYPE_HEADING
            is Skill -> VIEW_TYPE_SKILL
            else -> throw IllegalArgumentException("Unknown item type")
        }
    }

    fun getSpanSize(position: Int): Int {
        return if (getItem(position) is Skill) 1 else 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADING -> HeadingViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    VIEW_TYPE_HEADING,
                    parent,
                    false
                )
            )
            VIEW_TYPE_SKILL -> SkillViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    VIEW_TYPE_SKILL,
                    parent,
                    false
                )
            )
            else -> throw IllegalStateException("There is no type like this!")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeadingViewHolder) holder.bindView(getItem(position) as SectionHeader)
        else (holder as SkillViewHolder).bindView(getItem(position) as Skill)
    }


    inner class HeadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(sectionHeader: SectionHeader) {
            itemView.skillsCategoryHeader.text = sectionHeader.parentCategory.title
//            if (sectionHeader.parentCategory.id == selectedCategoryId)
//                itemView.skillsCategoryHeader.setTextColor(Color.parseColor("#4768fd"))
//            else
//                itemView.skillsCategoryHeader.setTextColor(
//                    itemView.context.getAttributeColor(
//                        android.R.attr.textColorSecondary
//                    )
//                )
//
//            if (isClickable) {
//                itemView.skillsCategoryHeader.setOnClickListener {
//                    if (selectedCategoryId != sectionHeader.parentCategory.id)
//                        setSelectedCategoryId(sectionHeader.parentCategory.id!!)
//                    else
//                        setSelectedCategoryId(0)
//                }
//            }
        }
    }

    inner class SkillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(skill: Skill) {
            itemView.skillView.apply {
                text = skill.getText()
                color = skill.getColor()
                selectedTextColor = skill.getSelectedTextColor()
                isChecked = false
            }
            if (selectedCategories.contains(skill.category.id))
                itemView.skillView.animateCheckedAndInvoke(true) {}

            if (isClickable) {
                itemView.skillView.setOnClickListener {
                    val newChecked = !itemView.skillView.isChecked
                    when {
                        newChecked && !isMultipleCategoriesAllowed -> setSelectedCategoryId(skill.category.id!!)
                        newChecked && isMultipleCategoriesAllowed ->
                            itemView.skillView.animateCheckedAndInvoke(true) {
                                selectedCategories.add(skill.category.id!!)
                            }
                        else -> itemView.skillView.animateCheckedAndInvoke(false) {
                            if (!isMultipleCategoriesAllowed)
                                setSelectedCategoryId(0)
                            else
                                selectedCategories.remove(skill.category.id!!)
                        }
                    }
                }
            }
        }
    }
}

internal object SkillDiff : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any) = oldItem == newItem

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        // This method is only called if areItemsTheSame() returns true. For anything other than
        // TagFilter items, that check suffices for this one as well.
        return (oldItem as? Skill)?.isUiContentEqual(newItem as Skill) ?: true
    }
}

internal class SkillSpanSizeLookup(private val adapter: SkillsRecyclerAdapter) :
    GridLayoutManager.SpanSizeLookup() {

    override fun getSpanSize(position: Int) = adapter.getSpanSize(position)
}