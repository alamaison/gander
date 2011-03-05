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