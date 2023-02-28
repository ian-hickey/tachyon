package tachyon.transformer.bytecode.op

import tachyon.transformer.Position

class OpUnaryNumber(`var`: Variable?, value: Expression?, type: Short, operation: Int, start: Position?, end: Position?) : AbsOpUnary(`var`, value, type, operation, start, end), ExprNumber