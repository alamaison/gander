# Python is 'tending towards' static, lexical scoping but it has some dark
# corners that still exhibit dynamic behaviour.  This test is an example of
# that.
#
# The class variable 'i' is bound in two places: first in the global namespace
# and then in the class's local namespace once it has been assigned.  This is
# notably different from how it would work in a function which would disallow
# the use of the variable before the local assignment, forcing all uses to
# bind locally.
#
# We don't faithfully model this because it's just too complicated.  So the
# first use of 'i' in the class is modelled as binding to the local namespace
# as well

i = 42

class B:
	print i       # prints 42 so must have bound in global namespace
	i = "hello"   # set class namespace (global name unchanged after)
	print i       # prints hello so no longer looking at global namespace

print i           # prints 42 proving that assignment in class bound locally
