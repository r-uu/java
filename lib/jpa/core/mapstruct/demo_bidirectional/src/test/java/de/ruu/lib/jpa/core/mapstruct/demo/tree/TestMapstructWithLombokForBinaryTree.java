package de.ruu.lib.jpa.core.mapstruct.demo.tree;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestMapstructWithLombokForBinaryTree
{
	@Test void createBinTreeWithSingleNodeForDTOs()
	{
		NodeDTO root = new NodeDTO("root");

		assertThat(root.children().isEmpty()).isEqualTo(true);
	}
}