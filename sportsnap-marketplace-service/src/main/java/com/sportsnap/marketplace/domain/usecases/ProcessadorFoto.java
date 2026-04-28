package com.sportsnap.marketplace.domain.usecases;

/**
 * Pattern: Decorator
 *
 * Interface base para processamento de fotos.
 * Decoradores adicionam funcionalidades (marca d'agua, resize)
 * sem modificar a implementacao base.
 */
public interface ProcessadorFoto {

    String processar(String caminhoOriginal);

    String getDescricao();
}
