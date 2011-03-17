class my_class_empty:
	pass

class my_class:
	def my_method_empty(self):
		pass
	
def my_free_function():
	pass

def test_nesting():
	def my_nested_def():
		pass

def test_nesting_class():
	
	class nested_class:
		
		def __init__(self):
			pass
		
		class really_nested_class:
			pass
