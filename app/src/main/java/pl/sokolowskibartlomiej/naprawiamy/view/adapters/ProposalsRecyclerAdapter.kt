package pl.sokolowskibartlomiej.naprawiamy.view.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_proposal.view.*
import pl.sokolowskibartlomiej.naprawiamy.R
import pl.sokolowskibartlomiej.naprawiamy.model.ListingProposal
import pl.sokolowskibartlomiej.naprawiamy.model.UserWithVotes
import pl.sokolowskibartlomiej.naprawiamy.utils.format
import pl.sokolowskibartlomiej.naprawiamy.view.fragments.ListingDetailsFragment

class ProposalsRecyclerAdapter(
    val fragment: ListingDetailsFragment,
    val proposalAccepted: Boolean
) :
    RecyclerView.Adapter<ProposalsRecyclerAdapter.ProposalViewHolder>() {

    private var mProposals = listOf<ListingProposal>()
    private var mSpecialists = listOf<UserWithVotes>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProposalViewHolder =
        ProposalViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_proposal,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ProposalViewHolder, position: Int) =
        holder.bindView(mProposals[position], mSpecialists)

    override fun getItemCount(): Int = mProposals.size

    fun setProposalsList(list: List<ListingProposal>) {
        mProposals = list
        notifyDataSetChanged()
    }

    fun setSpecialistsList(list: List<UserWithVotes>) {
        mSpecialists = list
        notifyDataSetChanged()
    }


    inner class ProposalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindView(proposal: ListingProposal, specialists: List<UserWithVotes>) {
            val specialist = specialists.firstOrNull { it.user.id == proposal.specialistId }
            itemView.proposalSpecialistName.text =
                "${specialist?.user?.firstName ?: ""} ${specialist?.user?.lastName ?: ""}"
            itemView.proposalValue.text = proposal.offeredValue.toString()
            itemView.proposalDeadline.text = proposal.offeredDeadline.format()
            itemView.acceptProposalBtn.visibility =
                if (proposalAccepted) View.GONE else View.VISIBLE

            if (!specialist?.votes.isNullOrEmpty()) {
                val rating =
                    specialist!!.votes.map { it.rating }.sum().toDouble() / specialist.votes.size
                itemView.proposalRating.text = "${rating}/5"
            } else {
                itemView.proposalRating.text = "0/5"
            }

            if (proposalAccepted) itemView.setBackgroundColor(Color.parseColor("#8BC34A"))

            itemView.acceptProposalBtn.setOnClickListener {
                fragment.acceptProposal(proposal.id!!)
            }
        }
    }
}