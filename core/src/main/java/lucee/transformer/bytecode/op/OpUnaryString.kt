package lucee.transformer.bytecode.op

import lucee.transformer.Position

class OpUnaryString(`var`: Variable?, value: Expression?, type: Short, op: Int, start: Position?, end: Position?) : AbsOpUnary(`var`, value, type, op, start, end), ExprString