package lucee.transformer.bytecode.op

import lucee.transformer.Position

class OpUnaryNumber(`var`: Variable?, value: Expression?, type: Short, operation: Int, start: Position?, end: Position?) : AbsOpUnary(`var`, value, type, operation, start, end), ExprNumber