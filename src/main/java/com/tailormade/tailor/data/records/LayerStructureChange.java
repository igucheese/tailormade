package com.tailormade.tailor.data.records;

import java.util.List;

public record LayerStructureChange(List<LayerData> beforeLayers, List<LayerData> afterLayers) implements EditorActions {
}
