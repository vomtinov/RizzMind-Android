package com.rizzmind.android.service

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.rizzmind.android.R
import com.rizzmind.android.suggestor.Suggestion

class SuggestionsAdapter(
    private val suggestions: List<Suggestion>,
    private val onSuggestionSelected: (Suggestion) -> Unit
) : RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder>() {

    class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val suggestionText: TextView = itemView.findViewById(R.id.suggestion_text)
        val suggestionSource: TextView = itemView.findViewById(R.id.suggestion_source)
        val copyButton: MaterialButton = itemView.findViewById(R.id.copy_button)
        val suggestionCard: MaterialCardView = itemView.findViewById(R.id.suggestion_card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.suggestion_item, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val suggestion = suggestions[position]
        
        holder.suggestionText.text = suggestion.text
        holder.suggestionSource.text = "via ${suggestion.source}"
        
        // Set confidence-based styling
        val alpha = (suggestion.confidence * 0.7f + 0.3f).coerceIn(0.3f, 1.0f)
        holder.suggestionCard.alpha = alpha
        
        holder.copyButton.setOnClickListener {
            onSuggestionSelected(suggestion)
        }
        
        holder.suggestionCard.setOnClickListener {
            onSuggestionSelected(suggestion)
        }
    }

    override fun getItemCount(): Int = suggestions.size
}