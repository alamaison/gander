# this module also calls an f but not the one we are after. this tests that
# the analysis doesn't get distracted and that it does pay attention to what
# the LHS of the attribute access is
def f(y):
	pass


