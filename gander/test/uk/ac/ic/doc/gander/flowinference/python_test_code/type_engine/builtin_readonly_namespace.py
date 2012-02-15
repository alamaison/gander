# builtin object's have read-only namespaces so we should look for
# assignments to their attibutes.  This should improve both speed and precision.
#
# This is difficult to test for.  Here we do it by assigning to a builtin and
# checking that it was ignored.  In reality, it would raise an error

def g(self):
	print self

object.f = g

print object.f # what_am_i