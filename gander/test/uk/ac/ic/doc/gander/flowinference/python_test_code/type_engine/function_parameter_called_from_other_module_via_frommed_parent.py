# Function f is called from here and from the _aux module. Both passed types
# must be included in the inferred type of the parameter x. The version called
# in the other module has been imported there using the from-style import on
# its containing class and then accessed by attribute

# The important thing with this test is that the object imported via 'from'
# is not a module.

# This import is just to force the calling module to be loaded so the
# analysis will consider it
import function_parameter_called_from_other_module_via_frommed_parent_aux

class C:
	pass

def f(self, x):
	print x # what_am_i

C.f = f

f(None, "Hello")
