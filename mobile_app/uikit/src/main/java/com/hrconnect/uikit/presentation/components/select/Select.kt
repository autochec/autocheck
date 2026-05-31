package com.hrconnect.uikit.presentation.components.select

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hrconnect.uikit.R
import com.hrconnect.uikit.common.theme.HrTheme

/**
 * Компонент выбора одного значения из списка (селект/дропдаун).
 *
 * Ответственность:
 * - Отображение текущего выбранного элемента (selectedItem) и метки (label).
 * - Открытие модального bottom sheet со списком для выбора.
 * - Уведомление о выборе через колбэк onItemClick.
 * - Логирование открытия, закрытия и выбора элемента.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param items список доступных для выбора строк
 * @param onItemClick колбэк, вызываемый при выборе элемента (передаёт выбранную строку)
 * @param modifier внешний модификатор
 * @param selectedItem текущий выбранный элемент (null = ничего не выбрано)
 * @param label текст заголовка (null = скрыт)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Select(
    items: List<String>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedItem: String? = null,
    label: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    // Логирование открытия и закрытия модального окна (INFO)
    LaunchedEffect(expanded) {
        if (expanded) {
            Log.i("Select", "Пользователь открыл список выбора — label=$label")
        } else {
            // Логируем закрытие только если было открыто (чтобы не писать при инициализации)
            // Для простоты логируем всегда, но можно добавить условие.
            // Расширенный вариант: запомнить предыдущее состояние через rememberPrevious.
            Log.i("Select", "Список выбора закрыт — label=$label")
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.5.dp)
    ) {
        if (label != null) {
            Text(
                text = label,
                style = HrTheme.typography.fieldLabel
            )
        }
        // Поле ввода, имитирующее селект (кликабельная область)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = HrTheme.colorScheme.border,
                    shape = RoundedCornerShape(8.dp)
                )
                .background(HrTheme.colorScheme.background)
                .clickable {
                    expanded = true
                }
                .padding(
                    horizontal = 16.dp,
                    vertical = 11.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedItem != null) {
                Text(
                    text = selectedItem,
                    style = HrTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier.size(12.dp),
                imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_down),
                contentDescription = null,
                tint = HrTheme.colorScheme.secondary
            )
        }
    }

    // Модальный bottom sheet со списком вариантов
    if (expanded) {
        ModalBottomSheet(
            onDismissRequest = {
                expanded = false
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(items) { item ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Логирование выбора элемента (INFO) до вызова колбэка
                                Log.i(
                                    "Select",
                                    "Пользователь выбрал элемент — item=\"$item\", label=$label"
                                )
                                onItemClick(item)
                                expanded = false
                            }
                            .padding(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            ),
                        text = item,
                        style = HrTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SelectPreview() {
    HrTheme {
        Select(
            items = listOf(
                "Engineering",
                "Department 1",
                "Department 2",
                "Department 3"
            ),
            selectedItem = "Engineering",
            onItemClick = {}
        )
    }
}