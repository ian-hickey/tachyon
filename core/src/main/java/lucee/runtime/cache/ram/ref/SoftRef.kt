package lucee.runtime.cache.ram.ref

import java.lang.ref.SoftReference

class SoftRef<T>(referent: T?) : SoftReference<T?>(referent), Ref<T?>