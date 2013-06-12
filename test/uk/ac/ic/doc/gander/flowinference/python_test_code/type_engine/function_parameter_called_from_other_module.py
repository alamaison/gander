# Function f is called from here and from the _aux module. Both passed types
# must be included in the inferred type of the parameter x

# This import is just to force the calling module to be loaded so the
# analysis will consider it
import function_parameter_called_from_other_module_aux

def f(x):
	print x # what_am_i

f("Hello")

# The aux module calls this function too which tests that the analysis check
# what attribute the RHS actually referrs to
def distraction(x):
	print x