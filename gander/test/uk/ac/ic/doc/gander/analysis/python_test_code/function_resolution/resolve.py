def test_local_namespace():
	call_me_local("local_tag_expected")

def call_me_local(local_tag):
	pass

import sister

def test_imported():
	sister.sisterly("love_expected")

import children.daughter

def test_imported_from_package_module():
	children.daughter.call_her("emily_expected")

def test_imported_from_package():
	children.my_children_like("music_expected")

import children.grandchildren.grandson

def test_imported_from_subpackage_module():
	children.grandchildren.grandson.sweeties_please("werthers_expected")

from children.son import play_football

def test_from_import():
	play_football("cold_and_wet_expected")
	
def test_call_builtin():
	print len("iterable_expected")

def module_scope_function(scope_tag):
	pass

def common_function_name(module_tag):
	pass

class ClassScope:
	def test_resolution_in_method(self):
		module_scope_function("scope_tag_expected")
		
	def test_resolution_in_method_with_clashing_method_name(self):
		common_function_name("module_tag_expected")
		# should resolve to module function above, not clashing method below
			
	def common_function_name(class_tag):
		"""This method exists to check that name resolution in the method 
			above doesn't resolve the function to this method as it isn't 
			prefixed with 'self'"""
		pass
