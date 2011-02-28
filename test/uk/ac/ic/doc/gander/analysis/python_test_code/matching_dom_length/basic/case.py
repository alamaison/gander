def basic():
	y = x
	y.a()
	y.b("tag")
	y.a()

def kills_in_same_block():
	y = x
	y.a("tag1")
	y.b("tag2")
	y = z
	y.b("tag3")
	y.c("tag4")
	
def mixed_kills_in_same_block():
	x.a("tag1")
	y.a("tag2")
	x = z
	x.b("tag3")
	y.m("tag4")

def assign_value_is_use():
	x.a("tag1")
	x = x.b("tag2")
	x.a("tag3")

def multikill():
	y.a("tag1")
	x.b("tag2")
	x, y = y, x
	x.c("tag3")
	y.d("tag4")

def include_only_dominators():
	y.a()
	if x:
		y.b()
	y.c("tag")

def include_only_postdominators():
	y.a("tag")
	if x:
		y.b()
	y.c()

def include_dom_and_postdom():
	y.a()
	if x:
		if a:
			y.p()
		y.b("tag")
		if z:
			y.q()
	y.c()

def assignment_in_non_dom_block():
	y = bob()
	x = rob()
	y.a("tag1")
	y.b("tag2")
	if x:
		y = x
		y.e("tag5")
	y.c("tag3")
	y.d("tag4")

def assignment_considered_in_correct_order():
	x = x.m()
	x.a("tag1")
	
def while_phi():
	while x.b("tag"):
		x = z.bob("tag2")
	
def while_no_phi():
	while x.b("tag"):
		y = z.bob("tag2")
	
def while_non_loop_var_causes_phi():
	while x.b("tag"):
		y = z.bob("tag2")
	y.b()
	
def for_phi():
	for x in w.a("tag"):
		x.bob("tag2")
		x = z

def for_no_phi():
	for x in w.a("tag"):
		y = z.bob("tag2")

def for_non_loop_var_causes_phi():
	for x in w.a("tag"):
		y = z.bob("tag2")
	y.b()

# Taken from cement licensed under the MIT License
# Copyright (c) 2009-2010 BJ Dierkes
def find_loader(fullname):
    for importer in z.iter_importers("for_tag"):
        loader = importer.find_module(fullname)
        loader.call("tag")
        if loader is not None:
            return loader

    return None

def aug_assign():
	y = 2
	y.a("tag1")
	y += 4
	y.a("tag2")
	
# The parameters of one call may include further calls to the same object
def nested_calls():
	g = h
	g.e("outer_tag", g.f("inner_tag"))

# The function body in the nested function should be ignored as it isn't
# part of the test function; it is the declaration of the object being
# assigned to the variable 'nested'.
def function_def_is_variable_decl():
	def nested(x):
		y = x
		y.n()
		y.o("tag_nested")
		y.p()
	z = w
	z.a()
	z.b("tag1")
	z.c()
	nested.__call__("tag2")

# The body of a lambda should be ignored as it isn't part of the test function;
# it is the declaration of callable object, that's all'
def lambda_is_object_ignore_body():
	l = lambda y: [y.a("tag_in_lambda1") for x in y.b("tag_in_lambda2")]
	z = w
	z.a()
	z.b("tag1")
	z.c()
	l.__call__("tag2")
	l.p()

def callme(z):
	z.p()
	z.q()

def recurse_into_call():
	y.a()
	y.b("tag")
	callme(y)

def callme_not_all_postdom(w):
	if w.r():
		w.s()
	w.t()

def recurse_into_call_with_some_non_postdom_statements():
	g.a()
	g.b("tag")
	callme_not_all_postdom(g)
	
def call_me_and_i_call_someone_else(t):
	t.bob()
	callme_not_all_postdom(t)
	if (t.sally()):
		callme(t)

def recurse_two_levels():
	y = x
	call_me_and_i_call_someone_else(y)
	y.a("tag")

import sibling_module

def recurse_into_call_imported_from_sibling_module():
	y = x
	sibling_module.call_me_my_brother(y)
	y.a("tag")

def recurse_into_call_imported_from_sibling_module_deep():
	y = x
	sibling_module.call_me_my_brother_deep(y)
	y.a("tag")

import subpackage.submodule

def recurse_into_call_imported_from_package_module():
	y = x
	subpackage.submodule.call_me_auntie(y)
	y.a("tag")

def recurse_into_call_imported_from_package_module_clever():
	y = x
	subpackage.submodule.call_me_auntie_and_see_how_clever_i_am(y)
	y.a("tag")

