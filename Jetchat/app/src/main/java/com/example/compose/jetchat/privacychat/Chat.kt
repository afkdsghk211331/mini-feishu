package com.example.compose.jetchat.privacychat

import android.content.ClipDescription
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose.jetchat.FunctionalityNotAvailablePopup
import com.example.compose.jetchat.R
import com.example.compose.jetchat.components.JetchatAppBar
import com.example.compose.jetchat.conversation.AuthorAndTextMessage
import com.example.compose.jetchat.conversation.DayHeader
import com.example.compose.jetchat.conversation.JumpToBottom
import com.example.compose.jetchat.conversation.JumpToBottomThreshold
import com.example.compose.jetchat.conversation.Message
import com.example.compose.jetchat.conversation.UserInput
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PrivateConversationContent(
    uiState: PrivateChatUiState,
    navigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {}
) {
    val authorMe = stringResource(R.string.author_me)
    val timeNow = stringResource(id = R.string.now)
    val scrollState = rememberLazyListState()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scope = rememberCoroutineScope()

    // 保持原始拖拽功能
    var background by remember { mutableStateOf(Color.Transparent) }
    var borderStroke by remember { mutableStateOf(Color.Transparent) }
    val dragAndDropCallback = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val clipData = event.toAndroidDragEvent().clipData
                if (clipData.itemCount < 1) return false
                uiState.addMessage(Message(authorMe, clipData.getItemAt(0).text.toString(), timeNow))
                return true
            }
            override fun onStarted(event: DragAndDropEvent) { borderStroke = Color.Red }
            override fun onEntered(event: DragAndDropEvent) { background = Color.Red.copy(alpha = .3f) }
            override fun onExited(event: DragAndDropEvent) { background = Color.Transparent }
            override fun onEnded(event: DragAndDropEvent) {
                background = Color.Transparent
                borderStroke = Color.Transparent
            }
        }
    }

    Scaffold(
        topBar = {
            PrivateChatTopBar(
                contactName = uiState.contactName,
                contactAvatar = uiState.contactAvatar,
                onBackPressed = onBackPressed,
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = background)
                .border(width = 2.dp, color = borderStroke)
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { event ->
                        event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                    },
                    target = dragAndDropCallback
                )
        ) {
            PrivateMessages(
                messages = uiState.messages,
                navigateToProfile = navigateToProfile,
                modifier = Modifier.weight(1f),
                scrollState = scrollState
            )
            UserInput(
                onMessageSent = { content ->
                    uiState.addMessage(Message(authorMe, content, timeNow))
                    scope.launch { scrollState.animateScrollToItem(0) }
                },
                resetScroll = { scope.launch { scrollState.scrollToItem(0) } },
                modifier = Modifier.navigationBarsPadding().imePadding()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateChatTopBar(
    contactName: String,
    contactAvatar: Int,
    onBackPressed: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier
) {
    var functionalityNotAvailablePopupShown by remember { mutableStateOf(false) }
    if (functionalityNotAvailablePopupShown) {
        FunctionalityNotAvailablePopup { functionalityNotAvailablePopupShown = false }
    }

    JetchatAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        onNavIconPressed = onBackPressed,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(contactAvatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(contactName)
            }
        },
        actions = {
            Icon(
                imageVector = Icons.Outlined.Call,
                modifier = Modifier
                    .clickable { functionalityNotAvailablePopupShown = true }
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                contentDescription = "video_call"
            )
            Icon(
                imageVector = Icons.Outlined.Info,
                modifier = Modifier
                    .clickable { functionalityNotAvailablePopupShown = true }
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                contentDescription = "contact_info"
            )
        }
    )
}

@Composable
fun PrivateMessages(
    messages: List<Message>,
    navigateToProfile: (String) -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    Box(modifier = modifier) {


        LazyColumn(
            reverseLayout = true,
            state = scrollState
        ) {
            itemsIndexed(items = messages) { index, message ->
                val prevAuthor = messages.getOrNull(index - 1)?.author
                val nextAuthor = messages.getOrNull(index + 1)?.author
                if (index == messages.size - 1) {
                    DayHeader("today")
                }

                PrivateMessage(
                    message = message,
                    isUserMe = message.author == "me",
                    isFirstMessageByAuthor = prevAuthor != message.author,
                    isLastMessageByAuthor = nextAuthor != message.author,
                    onAuthorClick = navigateToProfile
                )
            }
        }

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                scrollState.animateScrollToItem(0)
            }
        }

        val jumpThreshold = with(LocalDensity.current) { JumpToBottomThreshold.toPx() }
        val jumpToBottomEnabled by remember {
            derivedStateOf {
                scrollState.firstVisibleItemIndex != 0 ||
                        scrollState.firstVisibleItemScrollOffset > jumpThreshold
            }
        }

        JumpToBottom(
            enabled = jumpToBottomEnabled,
            onClicked = { scope.launch { scrollState.animateScrollToItem(0) } },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun PrivateMessage(
    message: Message,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    onAuthorClick: (String) -> Unit
) {
    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier.padding(top = 8.dp) else Modifier
    Row(
        modifier = spaceBetweenAuthors.then(
            if (isUserMe) Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)
            else Modifier.fillMaxWidth().wrapContentWidth(Alignment.Start)
        )
    ) {
        if (isLastMessageByAuthor) {
            Image(
                modifier = Modifier
                    .clickable { onAuthorClick(message.author) }
                    .padding(horizontal = 16.dp)
                    .size(42.dp)
                    .border(1.5.dp, MaterialTheme.colorScheme.tertiary, CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .clip(CircleShape),
                painter = painterResource(id = message.authorImage),
                contentDescription = null
            )
        } else {
            Spacer(modifier = Modifier.width(74.dp))
        }

        AuthorAndTextMessage(
            msg = message,
            isUserMe = isUserMe,
            isFirstMessageByAuthor = isFirstMessageByAuthor,
            isLastMessageByAuthor = isLastMessageByAuthor,
            authorClicked = onAuthorClick,
            modifier = Modifier.padding(end = 16.dp).weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPrivateConversation() {
    val uiState = remember {
        PrivateChatUiState(
            contactName = "John Doe",
            contactAvatar = R.drawable.someone_else,
            isOnline = true,
            initialMessages = listOf(
                Message("John Doe", "Hello!", "12:30 PM")
            )
        )
    }

    val scrollState = rememberLazyListState()

    PrivateConversationContent(
        uiState = uiState,
        navigateToProfile = { },
        onBackPressed = { },
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}
