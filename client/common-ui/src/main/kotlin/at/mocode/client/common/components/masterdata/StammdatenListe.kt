package at.mocode.client.common.components.masterdata

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.mocode.masterdata.domain.model.LandDefinition

/**
 * Compose component that displays master data (Stammdaten).
 * This is a Compose-based replacement for the React-based StammdatenListe component.
 * Currently focuses on countries (LandDefinition) but can be extended for other master data types.
 */
@Composable
fun StammdatenListe(
    countries: List<LandDefinition> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onCountryClick: (LandDefinition) -> Unit = {}
) {
    // UI rendering with Compose
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Stammdaten",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Länder",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            countries.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Keine Länder verfügbar",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(countries) { country ->
                        CountryCard(country = country, onClick = { onCountryClick(country) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryCard(
    country: LandDefinition,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = country.nameDeutsch,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ISO codes
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌍",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ISO-Codes: ${country.isoAlpha2Code} / ${country.isoAlpha3Code}",
                    style = MaterialTheme.typography.bodyMedium
                )
                country.isoNumerischerCode?.let { numCode ->
                    Text(
                        text = " / $numCode",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // English name if available
            country.nameEnglisch?.let { englishName ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🇬🇧",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Englischer Name: $englishName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // EU/EWR membership
            val membershipInfo = mutableListOf<String>()
            country.istEuMitglied?.let { isEuMember ->
                if (isEuMember) membershipInfo.add("EU-Mitglied")
            }
            country.istEwrMitglied?.let { isEwrMember ->
                if (isEwrMember) membershipInfo.add("EWR-Mitglied")
            }

            if (membershipInfo.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🇪🇺",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Mitgliedschaft: ${membershipInfo.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Status
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ℹ️",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Status: ${if (country.istAktiv) "Aktiv" else "Inaktiv"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Sort order if available
            country.sortierReihenfolge?.let { sortOrder ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔢",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sortierreihenfolge: $sortOrder",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Coat of arms/flag URL if available
            country.wappenUrl?.let { flagUrl ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏴",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Wappen/Flagge: $flagUrl",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * A badge that displays the country's EU/EWR membership status
 */
@Composable
fun CountryMembershipBadge(country: LandDefinition) {
    val membership = when {
        country.istEuMitglied == true -> "EU"
        country.istEwrMitglied == true -> "EWR"
        else -> null
    }

    membership?.let {
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = it,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
