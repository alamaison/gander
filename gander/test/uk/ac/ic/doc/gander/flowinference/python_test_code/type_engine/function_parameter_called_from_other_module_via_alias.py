# Function f is called from here and from the _aux module. Both passed types
# must be included in the inferred type of the parameter x.  This call in the
# other module doesn't use the imported module name but, instead, first
# aliases the imported module and then calls f via that name.  This tests that
# we follow namespaces properly when looking for accesses to a namespace's
# members

# This import is just to force the calling module to be loaded so the
# analysis will consider it
import function_parameter_called_from_other_module_via_alias_aux

def f(x):
	print x # what_am_i

f("Hello")
