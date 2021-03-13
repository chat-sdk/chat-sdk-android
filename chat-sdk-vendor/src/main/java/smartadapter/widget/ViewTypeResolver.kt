package smartadapter.widget

/*
 * Created by Manne Öhlund on 02/04/17.
 * Copyright © 2017 All rights reserved.
 */

import smartadapter.Position
import smartadapter.SmartViewHolderType

/**
 * Type alias custom lambda listener for resolving of view type by source data.
 */
typealias ViewTypeResolver = (item: Any, position: Position) -> SmartViewHolderType?
