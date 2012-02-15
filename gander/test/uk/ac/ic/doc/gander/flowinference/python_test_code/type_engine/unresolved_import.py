# test that an unresolved import doesn't kill typing completely
import bob

class A:
	pass

print A # what_am_i