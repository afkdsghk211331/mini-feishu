package com.example.compose.jetchat.privacychat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.compose.jetchat.MainViewModel

import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.os.bundleOf
import androidx.navigation.findNavController

import com.example.compose.jetchat.R
import com.example.compose.jetchat.conversation.ConversationContent
import com.example.compose.jetchat.data.exampleChatUiState
import com.example.compose.jetchat.theme.JetchatTheme

class PrivateConversationFragment : Fragment() {

    private val activityViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(inflater.context).apply {
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)

        setContent {
            JetchatTheme {
                PrivateConversationContent(
                    uiState = exampleChatUiState,
                    navigateToProfile = { user ->
                        val bundle = bundleOf("userId" to user)
                        findNavController().navigate(
                            R.id.nav_profile,
                            bundle
                        )
                    },
                    onBackPressed = {
                        activityViewModel.openDrawer()
                    }
                )
            }
        }
    }
}