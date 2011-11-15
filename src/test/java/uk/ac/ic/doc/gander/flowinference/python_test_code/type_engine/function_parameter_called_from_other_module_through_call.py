# Function f is called from here and from the _aux module. Both passed types
# must be included in the inferred type of the parameter x.
# The call in the other module is indirected via a call expression

# This import is just to force the calling module to be loaded so the
# analysis will consider it
import function_parameter_called_from_other_module_through_call_aux

def f(x):
	print x # what_am_i

f("Hello")
