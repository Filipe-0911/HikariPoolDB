package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.domain.cliente.Cliente;
import br.com.alura.bytebank.domain.cliente.DadosCadastroCliente;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ContaDAO {
    private Connection conn;
    ContaDAO(Connection connection) {
        this.conn = connection;
    }

    public void salvar(DadosAberturaConta dadosDaConta) {
        Cliente cliente = new Cliente(dadosDaConta.dadosCliente());
        Conta conta = new Conta(dadosDaConta.numero(), cliente);

        String sql = "INSERT INTO conta (numero, saldo, cliente_nome, cliente_cpf, cliente_email)" +
                "VALUES (?, ?, ?, ?, ?)";

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, conta.getNumero());
            preparedStatement.setBigDecimal(2, BigDecimal.ZERO);
            preparedStatement.setString(3, dadosDaConta.dadosCliente().nome());
            preparedStatement.setString(4, dadosDaConta.dadosCliente().cpf());
            preparedStatement.setString(5, dadosDaConta.dadosCliente().email());
            preparedStatement.execute();
            conn.close();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("Erro: " + e);
            throw new RuntimeException(e);
        }
    }

    public Set<Conta> listaDeContas () {
        String sql = "SELECT * FROM conta";
        Set<Conta> setContas = new HashSet<>();
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            preparedStatement = conn.prepareStatement(sql);
            rs = preparedStatement.executeQuery();

            while (rs.next()) {
                Integer numeroDaConta = rs.getInt(1);
                BigDecimal saldo = rs.getBigDecimal(2);
                String nome = rs.getString(3);
                String cpf = rs.getString(4);
                String email = rs.getString(5);
                boolean estaAtiva = rs.getBoolean(6);
                DadosCadastroCliente dc = new DadosCadastroCliente(nome, cpf, email);
                Cliente cl = new Cliente(dc);
                Conta conta = new Conta(numeroDaConta, cl);
                conta.setSaldo(saldo);
                conta.setEstaAtiva(estaAtiva);
                setContas.add(conta);
            }
            rs.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return setContas;
    }

    public Conta contaEspecifica (Integer idConta) {
        String sql = "SELECT * FROM conta WHERE numero=?";
        ResultSet rs = null;
        PreparedStatement ps = null;
        Conta conta = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idConta);
            rs = ps.executeQuery();

            while (rs.next()) {
                Integer numeroRecuperado = rs.getInt(1);
                BigDecimal saldo = rs.getBigDecimal(2);
                String nome = rs.getString(3);
                String cpf = rs.getString(4);
                String email = rs.getString(5);
                DadosCadastroCliente dadosCadastroCliente = new DadosCadastroCliente(nome, cpf, email);
                Cliente cliente = new Cliente(dadosCadastroCliente);
                conta = new Conta(numeroRecuperado, cliente);
                conta.setSaldo(saldo);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
        return conta;
    }

    public void alterarConta(Integer numero, BigDecimal valor) {
        PreparedStatement ps;
        String sql = "UPDATE conta SET saldo = ? WHERE numero = ?";

        try {
            conn.setAutoCommit(false);

            ps = conn.prepareStatement(sql);

            ps.setBigDecimal(1, valor);
            ps.setInt(2, numero);

            ps.execute();
            conn.commit();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    public void sacar(Conta conta, BigDecimal valor) {
        BigDecimal novoSaldo = conta.getSaldo().subtract(valor);

        String sql = "UPDATE conta SET saldo = ? where numero = ?";
        PreparedStatement ps = null;
        Integer numero = conta.getNumero();

        try {
            ps = conn.prepareStatement(sql);
            ps.setBigDecimal(1, novoSaldo);
            ps.setInt(2, numero);

            ps.execute();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deletarConta(Integer numeroDaConta) {

        String sql = """
                UPDATE conta SET esta_ativa = 0 WHERE numero = ?;
                """;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, numeroDaConta);
            ps.execute();
            ps.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
