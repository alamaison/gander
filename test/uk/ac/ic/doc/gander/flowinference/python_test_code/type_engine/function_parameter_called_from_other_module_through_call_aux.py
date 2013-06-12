import function_parameter_called_from_other_module_through_call

def get_callable(a):
	return function_parameter_called_from_other_module_through_call.f

get_callable([])(42) # should include this int
get_callable({}) # should not include this dict


