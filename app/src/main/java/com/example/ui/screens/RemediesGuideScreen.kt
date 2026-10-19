package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HomeoRemedy
import com.example.ui.HomeoCareViewModel

@Composable
fun RemediesGuideScreen(
    viewModel: HomeoCareViewModel,
    modifier: Modifier = Modifier
) {
    val remedies = viewModel.homeoRemedies
    val selectedRemedy by viewModel.selectedRemedy.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("remedies_guide_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Homeopathy Health Guide",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Classical principles, dosage rules, and constitutional remedies",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. Classical Principles Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🌿", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Foundations of Classical Homeopathy",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PrincipleBullet(
                        title = "1. Law of Similars (\"Like Cures Like\")",
                        desc = "A substance that produces specific symptoms in a healthy individual in large doses can cure similar symptoms in an ailing person when prepared in potentised dilution."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PrincipleBullet(
                        title = "2. Single Remedy Individualization",
                        desc = "Two patients with the same illness (e.g. Migraine) receive entirely different remedies depending on individual mental temperament, thirst, sleep, and weather sensitivity."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PrincipleBullet(
                        title = "3. The Minimum Dynamic Dose",
                        desc = "Medicines are prepared through serial potentization (dynamization), releasing energetic curative power without toxic chemical residue or side effects."
                    )
                }
            }
        }

        // 2. Dosage Rules & Don'ts
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rules for Taking Homeopathic Medicines",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DosageRuleItem(emoji = "👅", rule = "Dissolve sweet globules under the tongue — never swallow whole with water.")
                    DosageRuleItem(emoji = "🥄", rule = "Never touch globules with bare fingers. Tap into bottle cap or clean dry plastic spoon.")
                    DosageRuleItem(emoji = "☕", rule = "Avoid raw onion, raw garlic, strong coffee, mint, or camphor 30 minutes before and after taking medicine.")
                    DosageRuleItem(emoji = "☀️", rule = "Keep medicine vials away from direct heat, sunlight, microwaves, and strong perfumes.")
                }
            }
        }

        // 3. Prominent Remedies Directory Header
        item {
            Column {
                Text(
                    text = "Key Classical Remedies Directory",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap any remedy to view full constitutional keynote modalities",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(remedies) { remedy ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectRemedy(remedy) }
                    .testTag("remedy_card_${remedy.name}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = remedy.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = remedy.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Common Source: ${remedy.commonName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Key Indications: ${remedy.primaryIndications.joinToString(", ")}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Modalities: ${remedy.keynoteModalities}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }

    // Remedy Detail Modal
    if (selectedRemedy != null) {
        val rem = selectedRemedy!!
        AlertDialog(
            onDismissRequest = { viewModel.selectRemedy(null) },
            icon = {
                Icon(
                    imageVector = Icons.Default.LocalPharmacy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = rem.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = rem.commonName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                Column {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Category: ${rem.category}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Primary Indications:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    rem.primaryIndications.forEach { ind ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(text = "• ", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text(text = ind, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Keynote Modalities (Worse / Better):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = rem.keynoteModalities, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Clinical Prescribing Pearls:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = rem.clinicalAdvice, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.selectRemedy(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun PrincipleBullet(title: String, desc: String) {
    Column {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f), lineHeight = 15.sp)
    }
}

@Composable
fun DosageRuleItem(emoji: String, rule: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Text(text = emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = rule, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 16.sp)
    }
}
