package com.identic.fluentforge.ui.screens.reader.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.identic.fluentforge.ui.screens.reader.activities.ReaderActivity
import com.identic.fluentforge.ui.screens.reader.viewmodels.ReaderViewModel
import com.identic.fluentforge.R
import com.identic.fluentforge.dataReader.remote.epub.BookTextMapper
import com.identic.fluentforge.dataReader.remote.epub.models.EpubChapter
import com.identic.fluentforge.dataReader.remote.utils.noRippleClickable
import com.identic.fluentforge.ui.theme.FluentForgeTheme

@ExperimentalMaterialApi
@ExperimentalMaterial3Api
class ReaderRVAdapter(
    private val activity: ReaderActivity,
    private val viewModel: ReaderViewModel,
    private val clickListener: ReaderClickListener
) :
    RecyclerView.Adapter<ReaderRVAdapter.ReaderComposeViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<EpubChapter>() {
        override fun areItemsTheSame(oldItem: EpubChapter, newItem: EpubChapter): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: EpubChapter, newItem: EpubChapter): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var allChapters: List<EpubChapter>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    inner class ReaderComposeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val composeView: ComposeView = view.findViewById(R.id.ReaderRVItem)
        fun bind(position: Int, onClick: () -> Unit) {
            val chapter = allChapters[position]
            composeView.setContent {
                FluentForgeTheme(settingsViewModel = activity.settingsViewModel) {
                    SelectionContainer {
                        ReaderItem(chapter = chapter, viewModel = viewModel, onClick = onClick)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReaderComposeViewHolder {
        return ReaderComposeViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.reader_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return allChapters.size
    }

    override fun onBindViewHolder(holder: ReaderComposeViewHolder, position: Int) {
        holder.bind(position, onClick = { clickListener.onReaderClick() })
    }
}

@Composable
private fun ReaderItem(
    chapter: EpubChapter,
    viewModel: ReaderViewModel,
    onClick: () -> Unit
) {
    val paragraphs = chapter.body
        .splitToSequence("\n\n")
        .filter { it.isNotBlank() }
        .toList()

    val epubBook = viewModel.state.epubBook

    Column(modifier = Modifier
        .fillMaxWidth()
        .noRippleClickable { onClick() }) {
        Text(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 10.dp),
            text = chapter.title,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontFamily = viewModel.readerFont.value.fontFamily,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f)
        )
        Spacer(modifier = Modifier.height(15.dp))

        paragraphs.forEach { para ->
            when (val imgEntry = BookTextMapper.ImgEntry.fromXMLString(para)) {
                null -> {
                    val fontSize = (viewModel.textSize.value / 10) * 1.8
                    Text(
                        text = para,
                        fontSize = fontSize.sp,
                        lineHeight = 1.3.em,
                        fontFamily = viewModel.readerFont.value.fontFamily,
                        modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 8.dp),
                    )
                }

                else -> {
                    val image = epubBook!!.images.find { it.absPath == imgEntry.path }
                    image?.let {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(image.image)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        Divider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
            thickness = 2.dp,
        )
    }
}
