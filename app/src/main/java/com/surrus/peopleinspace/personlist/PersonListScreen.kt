@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalMaterialApi::class
)

package com.surrus.peopleinspace.personlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.pullrefresh.*
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.surrus.common.remote.Assignment
import com.surrus.peopleinspace.R
import com.surrus.peopleinspace.ui.PersonProvider
import com.surrus.peopleinspace.ui.PurpleGray50
import com.surrus.peopleinspace.ui.component.PeopleInSpaceGradientBackground
import com.surrus.peopleinspace.ui.component.PeopleInSpaceTopAppBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

const val PersonListTag = "PersonList"


@Composable
fun PersonListRoute(
    navigateToPerson: (String) -> Unit,
    viewModel: PersonListViewModel = getViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PersonListScreen(uiState, navigateToPerson, onRefresh = {
        viewModel.refresh()
    })

}

@Composable
fun PersonListScreen(
    uiState: PersonListUiState,
    navigateToPerson: (String) -> Unit,
    onRefresh: () -> Unit
) {
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    fun refresh() = refreshScope.launch {
        refreshing = true
        delay(500)
        onRefresh()
        refreshing = false
    }

    val state = rememberPullRefreshState(refreshing, ::refresh)

    PeopleInSpaceGradientBackground {
        Scaffold(
            topBar = {
                PeopleInSpaceTopAppBar(
                    titleRes = R.string.people_in_space,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.semantics { contentDescription = "PeopleInSpace" }
                )
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->

            Box(Modifier.pullRefresh(state)) {
                LazyColumn(
                    modifier = Modifier
                        .testTag(PersonListTag)
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                        .fillMaxSize()
                ) {
                    if (!refreshing) {
                        items(uiState.items) { person ->
                            PersonView(person, navigateToPerson)
                        }
                    }
                }

                PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
fun PersonView(person: Assignment, personSelected: (person: String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { personSelected(person.name) })
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val personImageUrl = person.personImageUrl ?: ""
            if (personImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = person.personImageUrl,
                    contentDescription = person.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(color = PurpleGray50)
                )
            } else {
                Spacer(modifier = Modifier.size(60.dp))
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column {
                Text(text = person.name, style = TextStyle(fontSize = 20.sp))
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(text = person.craft, style = TextStyle(fontSize = 14.sp))
                }
            }
        }
    }
}

@Preview
@Composable
fun PersonViewPreview(@PreviewParameter(PersonProvider::class) person: Assignment) {
    MaterialTheme {
        PersonView(person, personSelected = {})
    }
}