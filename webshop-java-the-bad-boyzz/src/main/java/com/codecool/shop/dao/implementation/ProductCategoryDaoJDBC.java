package com.codecool.shop.dao.implementation;

import com.codecool.shop.connection.dbConnection;
import com.codecool.shop.dao.ProductCategoryDao;

import com.codecool.shop.model.ProductCategory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductCategoryDaoJDBC implements ProductCategoryDao {

	private static ProductCategoryDao instance = null;
	dbConnection myConn = dbConnection.getInstance();

	public ProductCategoryDaoJDBC() throws IOException {
	}


	public static ProductCategoryDao getInstance() throws IOException {
		if (instance == null) instance = new ProductCategoryDaoJDBC();
		return instance;
	}

	@Override
	public void add(ProductCategory category) {
		try (Connection conn = myConn.getConnection()) {
			assert conn != null;
			try (PreparedStatement stmt = conn.prepareStatement
					("INSERT INTO categories (id, name, department, description) " +
							"values ( ? ,? ,?)")) {
				stmt.setString(1, category.getName());
				stmt.setString(2, category.getDepartment());
				stmt.setString(3, category.getDescription());
				stmt.executeUpdate();
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	@Override
	public ProductCategory find(int id) {
		try (Connection conn = myConn.getConnection()) {
			assert conn != null;
			try (PreparedStatement stmt = conn.prepareStatement
					("SELECT * FROM categories WHERE id = ?;")) {
				stmt.setInt(1, id);
				ResultSet rs = stmt.executeQuery();
				if (rs.next()) {

					String name = rs.getString("name");
					String department = rs.getString("department");
					String description = rs.getString("description");

					return new ProductCategory(id, name, department, description);
				}
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		return null;
	}


	@Override
	public void remove(int id) {
		try (Connection conn = myConn.getConnection()) {
			assert conn != null;
			try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM categories WHERE id = ?");) {
				stmt.setInt(1, id);
				stmt.executeUpdate();
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	@Override
	public List<ProductCategory> getAll() throws SQLException {
		try (Connection conn = myConn.getConnection()) {
			assert conn != null;
			try (PreparedStatement stmt = conn.prepareStatement
					("SELECT * FROM categories")) {
				ResultSet rs = stmt.executeQuery();
				List<ProductCategory> categories = new ArrayList<>();

				while (rs.next()) {
					int id = rs.getInt("id");
					String name = rs.getString("name");
					String department = rs.getString("department");
					String description = rs.getString("description");

					categories.add(new ProductCategory(id, name, department, description));
				}
				return categories;
			} catch (SQLException throwables) {
				throwables.printStackTrace();
			}
			return null;
		}
	}
}
