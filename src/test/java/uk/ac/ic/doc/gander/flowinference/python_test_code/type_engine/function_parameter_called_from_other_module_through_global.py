# Function f is called from here and from the _aux module. Both passed types
# must be included in the inferred type of the parameter x.
# The call in the other module is indirected the global variable in this module.

# This tests that, not only can we follow flow of code object declarations 
# between modules, but that we can also do so for arbitrary variables.

# This import is just to force the calling module to be loaded so the
# analysis will consider it
import function_parameter_called_from_other_module_through_global_aux

def f(x):
	print x # what_am_i
	
G = f

f("Hello")
